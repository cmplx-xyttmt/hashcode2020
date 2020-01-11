# 1. TODO: Read input file
# 2. TODO: Prepare output folder and code for writing to output folder
# 3. TODO: Write code to analyze/evaluate how good the current solution is
# 4. TODO: For each solution, if it's better than the current solution in the output file or if the output file doesn't
#  exist, write the solution to the output file.
import sys


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


if __name__ == '__main__':
    if len(sys.argv) > 1:
        file_location = sys.argv[1].strip()
        with open(file_location, 'r') as file:
            input_data = file.read()
        lines = input_data.split('\n')
        _capacity = int(lines[0].split()[0])
        _items = [int(item) for item in lines[1].split()]
        result = knapsack(_items, _capacity)
        print(len(result[1]))
        print(' '.join(map(str, result[1])))
