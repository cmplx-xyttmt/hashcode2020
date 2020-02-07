import sys


class Endpoint:

    def __init__(self, ld):
        self.ld = ld
        self.cache_servers = list()

    def add_cache(self, cache_id, latency):
        self.cache_servers.append((cache_id, latency))

    def __str__(self):
        return "{" + str(self.ld) + ", " + str(self.cache_servers) + "}"

    def __repr__(self):
        return self.__str__()


class Request:

    def __init__(self, video_id, endpoint, num_requests, videos):
        self.video_id = video_id
        self.endpoint = endpoint
        self.num_requests = num_requests
        self.video_size = videos[video_id]

    def __gt__(self, other):
        return self.video_size * self.num_requests > other.video_size * other.num_requests

    def __str__(self):
        return "{" + str(self.video_id) + ", " + str(self.endpoint) + ", " + str(self.num_requests) + ", " + str(
            self.video_size) + "}"

    def __repr__(self):
        return self.__str__()


class Cache:

    def __init__(self, x):
        self.videos = list()
        self.capacity = x

    def can_add(self, video, videos):
        return sum(self.videos) + videos[video] <= self.capacity

    def add_video(self, video, videos):
        if self.can_add(video, videos):
            self.videos.append(video)


def assign_video_to_cache(endpoint, caches, video, videos):
    for cache in endpoint.cache_servers:
        caches[cache[0]].add_video(video, videos)


def solve(videos, endpoints, requests, caches):
    requests.sort()
    for request in requests:
        assign_video_to_cache(endpoints[request.endpoint], caches, request.video_id, videos)
    print_solution(caches)
    print(calc_score(caches, requests, endpoints))


def print_solution(caches):
    caches = list(filter(lambda c: len(c.videos) > 0, caches))
    print(len(caches))
    for cache in caches:
        print("{} {}".format(len(cache.videos), str(cache.videos).replace(", ", " ").replace("[", "").replace("]", "")))


def calc_score(caches, requests, endpoints):
    cost = 0
    total_requests = 0
    for request in requests:
        endpoint = endpoints[request.endpoint]
        best_cost = endpoint.ld
        for cache in endpoint.cache_servers:
            if request.video_id in caches[cache[0]].videos:
                best_cost = min(best_cost, cache[1])
        cost += (endpoint.ld - best_cost) * request.num_requests
        total_requests += request.num_requests
    return (cost * 1000) / total_requests


if __name__ == '__main__':
    if len(sys.argv) > 1:
        file_location = sys.argv[1].strip()
        with open(file_location, 'r') as file:
            input_data = file.read()
        lines = input_data.split("\n")
        (_v, _e, _r, _c, _x) = map(int, lines[0].split())
        _videos = list(map(int, lines[1].split()))
        _endpoints = list()
        line_number = 2
        for _ in range(_e):
            (_ld, _k) = map(int, lines[line_number].split())
            _endpoint = Endpoint(_ld)
            line_number += 1
            for _ in range(_k):
                (_cid, _lc) = map(int, lines[line_number].split())
                _endpoint.add_cache(_cid, _lc)
                line_number += 1
            _endpoints.append(_endpoint)
        _requests = list()
        for _ in range(_r):
            (rv, re, rn) = map(int, lines[line_number].split())
            _request = Request(rv, re, rn, _videos)
            _requests.append(_request)
            line_number += 1

        _caches = [Cache(_x) for _ in range(_c)]
        # print(_videos, _endpoints, _requests)
        solve(_videos, _endpoints, _requests, _caches)
