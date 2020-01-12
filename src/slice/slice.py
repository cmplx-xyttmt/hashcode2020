import sys
import random


def knapsack(items, capacity):
    n = len(items)
    dp = [0] * (capacity + 1)
    taken = [(-1, -1) for _ in range(capacity + 1)]
    for i in range(n):
        prev_dp = list(dp)
        for c in range(0, capacity + 1):
            if c - items[i] >= 0 and items[i] + prev_dp[c - items[i]] > prev_dp[c]:
                taken[c] = (i, c - items[i])
                dp[c] = prev_dp[c - items[i]] + items[i]

    sol = []
    curr = taken[capacity]
    while curr[1] != -1:
        sol.append(curr[0])
        curr = taken[curr[1]]
    sol.sort()
    return dp[capacity], sol


def greedy(items, capacity, index_to_exclude):
    items_value = 0
    sol = []
    index = len(items) - 1
    while index >= 0:
        if index not in index_to_exclude:
            items_value += items[index]
            if items_value >= capacity:
                items_value -= items[index]
                break
            sol.append(index)
        index -= 1
    return items_value, sol


def randomly_remove(items, capacity):
    index = min(len(items) - 1, 10**8/len(items))
    first = greedy(items, capacity, {-1})
    curr_best_sol = first[1]
    best_sol_value = first[0]
    while index >= 0:
        indices_to_exclude = random.sample(range(len(curr_best_sol)), k=20)
        indices_to_exclude = [curr_best_sol[i] for i in indices_to_exclude]
        new_sol = greedy(items, capacity, indices_to_exclude)
        sol_value = new_sol[0]
        sol = new_sol[1]
        if sol_value > best_sol_value:
            curr_best_sol = sol
            best_sol_value = sol_value
        index -= 1
    sys.stderr.write("sol_val: {}\n".format(best_sol_value))
    return best_sol_value, curr_best_sol


if __name__ == '__main__':
    if len(sys.argv) > 1:
        file_location = sys.argv[1].strip()
        with open(file_location, 'r') as file:
            input_data = file.read()
        lines = input_data.split('\n')
        _capacity = int(lines[0].split()[0])
        _items = [int(item) for item in lines[1].split()]
        if _capacity < 10000:
            result = knapsack(_items, _capacity)
        else:
            result = randomly_remove(_items, _capacity)
        print(len(result[1]))
        print(' '.join(map(str, result[1])))
