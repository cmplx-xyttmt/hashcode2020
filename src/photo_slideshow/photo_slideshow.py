import sys
import random


class Photo:

    def __init__(self, photo_id, orientation, tags):
        self.id = photo_id
        self.orientation = orientation
        self.tags = set(tags)

    def __str__(self):
        return "{" + str(self.id) + ": " + self.orientation + ", " + str(self.tags) + "}"

    def __repr__(self):
        return self.__str__()

    def __gt__(self, other):
        return len(self.tags) > len(other.tags)


class Slide:

    def __init__(self, photos, photo1, photo2=None):
        self.photo1 = photo1  # Id of the photo1
        self.orientation = photos[photo1].orientation
        self.photo2 = photo2
        self.tags = photos[self.photo1].tags
        if self.photo2 is not None:
            self.tags = self.tags.union(photos[self.photo2].tags)

    def interest_factor(self, other):
        common = len(self.tags.intersection(other.tags))
        this_diff = len(self.tags.difference(other.tags))
        other_diff = len(other.tags.difference(self.tags))
        return min(common, this_diff, other_diff)

    def __gt__(self, other):
        return len(self.tags.intersection(other.tags))
        # return len(self.tags) > len(other.tags)

    def __str__(self):
        return str(self.photo1) + ((" " + str(self.photo2)) if self.photo2 is not None else "")

    def __repr__(self):
        return self.__str__()


class Tag:

    def __init__(self, slide_ids):
        self.slide_ids = slide_ids
        self.num_slides = len(slide_ids)

    def __gt__(self, other):
        return len(self.slide_ids) > len(other.slide_ids)


def evaluate_solution(slides):
    score = 0
    for i in range(1, len(slides)):
        score += slides[i].interest_factor(slides[i - 1])
    return score


def solve(photos):
    horizontal_photos = list(filter(lambda _photo: _photo.orientation == 'H', photos))
    vertical_photos = list(filter(lambda _photo: _photo.orientation == 'V', photos))

    slides = []
    # for photo in horizontal_photos:
    #     slides.append(Slide(photos, photo.id))
    for photo in photos:
        slides.append(Slide(photos, photo.id))

    # vertical_photos.sort()
    # for i in range(0, len(vertical_photos)):
    #     j = len(vertical_photos) - i - 1
    #     if j <= i:
    #         break
    #     slides.append(Slide(photos, vertical_photos[i].id, vertical_photos[j].id))

    tags = dict()
    for i in range(len(slides)):
        slide = slides[i]
        for tag in slide.tags:
            if tag not in tags:
                tags[tag] = []
            tags[tag].append(i)
    for tag in tags:
        tags[tag] = Tag(tags[tag])
    # tags = [Tag(tags[tag]) for tag in tags]
    # sys.stderr.write("Num of tags: {}\n".format(len(tags)))
    # tags.sort(reverse=True)
    # seen = set()
    # solution = []
    # for tag in tags:
    #     seg = list(filter(lambda _slide_id: _slide_id not in seen, tag.slide_ids))
    #     for ss in seg:
    #         seen.add(ss)
    #     seg = [slides[i] for i in seg]
    #     solution += greedy(seg)
    # solution = greedy(slides)
    solution = greedy_next(slides, tags)
    # solution = two_opt(slides)
    sys.stderr.write("Avg num of slides per tag: {}\n".format(analysis(tags)))
    # Combine vertical slides
    real_solution = []
    curr_vertical = None
    for slide in solution:
        if slide.orientation == 'H':
            real_solution.append(slide)
        else:
            if curr_vertical is None:
                curr_vertical = slide
            else:
                real_solution.append(Slide(photos, curr_vertical.photo1, slide.photo1))
                curr_vertical = None

    return real_solution


def two_opt(slides):
    num_iterations = min(10 ** 5 / len(slides), len(slides))
    best_slides = slides
    best_score = evaluate_solution(slides)
    # sys.stderr.write(str(num_iterations) + "\n")
    for _ in range(num_iterations):
        i = random.randint(0, len(slides) - 1)
        j = random.randint(i + 1, len(slides))
        new_slides = best_slides[0:i] + list(reversed(best_slides[i:j])) + best_slides[j:]
        new_score = evaluate_solution(new_slides)
        if new_score > best_score:
            best_slides = new_slides
            best_score = new_score
    return best_slides


def greedy(slides):
    if len(slides) == 0:
        return []
    look_ahead = min(10 ** 7 / len(slides), len(slides))
    # sys.stderr.write("Look ahead: {}\n".format(look_ahead))
    seen = set()
    solution = []

    def next_indices(i):
        if i < look_ahead:
            return list(filter(lambda idx: idx not in seen,
                               list(range(i + 1, len(slides)))))
        return list(filter(lambda idx: idx not in seen,
                           random.sample(range(i + 1, len(slides)), min(look_ahead, len(slides) - i - 1))))

    for j in range(len(slides)):
        if j in seen:
            continue
        seen.add(j)
        nx_indices = next_indices(j)
        best_k = -1
        best_interest = -1
        for k in nx_indices:
            interest = slides[j].interest_factor(slides[k])
            if interest > best_interest:
                best_k = k
                best_interest = interest
        solution.append(slides[j])
        if len(slides) > best_k >= 0:
            seen.add(best_k)
            solution.append(slides[best_k])
    return solution


def analysis(tags):
    avg_slides = 0
    for tag in tags:
        avg_slides += tags[tag].num_slides
    return avg_slides/len(tags)


def greedy_next(slides, tags):
    solution = []
    seen = set()
    curr = slides[0]
    seen.add(0)
    idx = 0
    while len(solution) < len(slides):
        solution.append(curr)
        best_nxt = -1
        best_nxt_score = 0
        # sys.stderr.write(
        #     "Number of tags: {}\n".format(len(curr.tags)))
        nxts = set()
        for tag in curr.tags:
            if tags[tag].num_slides > 1000:
                continue
            nxts = nxts.union(set(tags[tag].slide_ids))
            nxts = nxts.difference(seen)
            if len(nxts) > 1000:
                break
        # nxts = list(filter(lambda x: x not in seen, nxts))
        # sys.stderr.write(
        #   "Number of neighbors to examine for {}: {} rem: {}\n".format(curr, len(nxts), len(slides) - len(solution)))
        for i in nxts:
            score = curr.interest_factor(slides[i])
            if score > best_nxt_score:
                best_nxt_score = score
                best_nxt = i
        if best_nxt == -1:
            while idx in seen:
                idx += 1
            if idx < len(slides):
                curr = slides[idx]
                seen.add(idx)
        else:
            seen.add(best_nxt)
            curr = slides[best_nxt]
    return solution


def print_solution(slides):
    print(len(slides))
    for slide in slides:
        print(slide)


if __name__ == '__main__':
    if len(sys.argv) > 1:
        file_location = sys.argv[1].strip()
        with open(file_location, 'r') as file:
            input_data = file.read()
        lines = input_data.split("\n")
        n = int(lines[0])
        line_number = 1
        _photos = []
        while line_number < n + 1:
            photo_data = lines[line_number].split(" ")
            _photos.append(Photo(line_number - 1, photo_data[0], photo_data[2:]))
            line_number += 1
        # print(_photos)
        _slides = solve(_photos)
        print_solution(_slides)
        sys.stderr.write("Score: {}\n".format(evaluate_solution(_slides)))
