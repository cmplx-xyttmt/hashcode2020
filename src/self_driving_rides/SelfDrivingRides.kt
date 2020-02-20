package self_driving_rides

import java.io.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

fun main() {
    solve("src/self_driving_rides/data/a_example.in", "src/self_driving_rides/output/a_example.out")
    solve("src/self_driving_rides/data/b_should_be_easy.in", "src/self_driving_rides/output/b_should_be_easy.out")
    solve("src/self_driving_rides/data/c_no_hurry.in", "src/self_driving_rides/output/c_no_hurry.out")
    solve("src/self_driving_rides/data/d_metropolis.in", "src/self_driving_rides/output/d_metropolis.out")
    solve("src/self_driving_rides/data/e_high_bonus.in", "src/self_driving_rides/output/e_high_bonus.out")
    println(totalScore)
}

fun solve(inputFile: String, outputFile: String) {
    inputReader = InputStreamReader(FileInputStream(File(inputFile)))
    val output = File(outputFile)
    output.createNewFile()
    outputWriter = OutputStreamWriter(FileOutputStream(output))
    println(inputFile)
    currLine = -1
    lines = inputReader.readLines()
    val (_, _, F, N, B, T) = readInts()
//    println("F: $F N: $N B: $B T: $T")
    steps = T
    bonus = B
    val rides = mutableListOf<Ride>()
    repeat(N) { id ->
        val (a, b, x, y, s, f) = readInts()
        rides.add(Ride(id, Intersection(a, b), Intersection(x, y), s, f))
    }
    inputReader.close()

    val vehicles = List(F) { Vehicle(it) }
//    vehicles[0].rides.add(rides[0])
//    vehicles[1].rides.add(rides[2])
//    vehicles[1].rides.add(rides[1])
    assignRides(rides, vehicles)
//    assignRidesPerVehicle(rides, vehicles)
    writeToOutput(vehicles)
    val score = calcScore(vehicles)
    totalScore += score
    println("Score: $score")
    println("=================================\n\n")
}

private var steps = -1
private var bonus = -1
private var totalScore = 0

private operator fun <E> List<E>.component6(): E {
    return this[5]
}

fun writeToOutput(vehicles: List<Vehicle>) {
    val solution = vehicles.sortedBy { it.id }
    solution.forEach { vehicle ->
        outputWriter.appendln(vehicle.rides.map { it.id }.joinToString(" "))
    }
    outputWriter.close()
}

/**
 * Sorting by latest start: 35632395
 * Sorting by length: 34011637
 * Sorting by earliest start: 33368995
 * Sorting by distance to origin: 30902669
 */
fun assignRides(rides: MutableList<Ride>, vehicles: List<Vehicle>) {
//    rides.sortBy { ride -> ride.latestStart } // Try other criteria such as distance, earliest start e.t.c

//    rides.sortByDescending { ride -> ride.length }

//    rides.sortBy { ride -> ride.earliestStart }

//    rides.sortBy { ride -> ride.start.distanceTo(Intersection(0,0)) }

//    rides.shuffle()
    var ridesFulfilled = 0
    rides.forEach { ride ->
        val scores =
            vehicles.map { vehicle ->
                Pair(scoreForTaking(vehicle, ride)*1000 - timeWasted(vehicle, ride),
                    vehicle)
            }.sortedByDescending { it.first }
//        val scores =
//            vehicles.map { vehicle -> Pair(timeWasted(vehicle, ride), vehicle) }.sortedBy { it.first }
//        val scores =
//            vehicles.map { vehicle ->
//                Pair(
//                    combinedScores(scoreForTaking(vehicle, ride), timeWasted(vehicle, ride)),
//                    vehicle
//                )
//            }.sortedByDescending { it.first }
//        if (curr == 1) println("${scores.map { it.first }} \n${ride.latestFinish} ${ride.start}")
        if (scores[0].first >= 0) {
            take(scores[0].second, ride)
            ridesFulfilled++
        }
    }

    val vehiclesUsed = vehicles.filter { it.rides.size > 0 }.size
    println("Vehicles used: $vehiclesUsed / ${vehicles.size}")
    println("Rides fulfilled: $ridesFulfilled / ${rides.size}")
}

fun assignRidesPerVehicle(rides: MutableList<Ride>, vehicles: List<Vehicle>) {
    var ridesFulfilled = 0
    val taken = MutableList(rides.size) { false }
    vehicles.forEach { vehicle ->
        val scores = rides.map { ride ->
            Pair(
                if (taken[ride.id]) Int.MAX_VALUE else vehicle.currIntersection.distanceTo(ride.end),
                ride
            )
        }.sortedBy { it.first }
        scores.forEach { score ->
            if (canTake(vehicle, score.second) && !taken[score.second.id]) {
                take(vehicle, score.second)
                taken[score.second.id] = true
                ridesFulfilled++
            }
        }
    }
    val vehiclesUsed = vehicles.filter { it.rides.size > 0 }.size
    println("Vehicles used: $vehiclesUsed / ${vehicles.size}")
    println("Rides fulfilled: $ridesFulfilled / ${rides.size}")
}

fun combinedScores(score: Int, timeWasted: Int): Double {
    if (score == -1 || timeWasted == Int.MAX_VALUE) return (-1).toDouble()
    if (timeWasted == 0) return score.toDouble()
    return score / timeWasted.toDouble()
}

fun scoreForTaking(vehicle: Vehicle, ride: Ride): Int {
    if (!canTake(vehicle, ride)) return -1
    var score = ride.length
    val startStep = vehicle.currStep + vehicle.currIntersection.distanceTo(ride.start)
    if (startStep <= ride.earliestStart) score += bonus
    return score
}

fun timeWasted(vehicle: Vehicle, ride: Ride): Int {
    if (!canTake(vehicle, ride)) return 10.toDouble().pow(6).toInt()
    return ride.earliestStart - vehicle.currStep
}

fun canTake(vehicle: Vehicle, ride: Ride): Boolean {
    val startStep = vehicle.currStep + vehicle.currIntersection.distanceTo(ride.start)
    return startStep <= ride.latestStart
}

fun take(vehicle: Vehicle, ride: Ride) {
    val stepsToRide = vehicle.currIntersection.distanceTo(ride.start)
    vehicle.currStep = max(vehicle.currStep + stepsToRide, ride.earliestStart)
    vehicle.currStep += ride.length
    vehicle.rides.add(ride)
}

fun calcScore(vehicles: List<Vehicle>): Int {
    return vehicles.map { calcScoreForVehicle(it) }.sum()
}

fun calcScoreForVehicle(vehicle: Vehicle): Int {
    var step = 0
    var score = 0
    vehicle.rides.forEach { ride ->
        step += vehicle.intersection.distanceTo(ride.start)
        if (step <= ride.earliestStart) score += bonus
        step = max(ride.earliestStart, step)
        if (step + ride.length <= ride.latestFinish) score += ride.length
        step += ride.length
//        println("Bonus: $bonus Ride length: ${ride.length}")
    }

    return score
}

data class Intersection(val x: Int, val y: Int) {

    fun distanceTo(other: Intersection): Int {
        return abs(other.x - x) + abs(other.y - y)
    }
}

data class Ride(
    val id: Int,
    val start: Intersection,
    val end: Intersection,
    val earliestStart: Int,
    val latestFinish: Int
) {
    val length = start.distanceTo(end)
    val latestStart = latestFinish - length
}

class Vehicle(val id: Int) {
    val rides = mutableListOf<Ride>()
    var intersection = Intersection(0, 0)
    var currIntersection = Intersection(0, 0)
    var currStep = 0
}


private lateinit var inputReader: InputStreamReader
private lateinit var outputWriter: OutputStreamWriter
private lateinit var lines: List<String>
private var currLine = -1
private fun readLn(): String {
    currLine++
    return lines[currLine]
}

private fun readStrings() = readLn().trim().split(" ")
private fun readInt() = readLn().toInt()
private fun readInts() = readStrings().map { it.toInt() }
