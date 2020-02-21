package googlebooks

import java.io.*
import kotlin.math.min

fun main() {
    solve("src/googlebooks/data/a_example.txt", "src/googlebooks/output/a.out")
    solve(
        "src/googlebooks/data/b_read_on.txt",
        "src/googlebooks/output/b.out"
    )
    solve(
        "src/googlebooks/data/c_incunabula.txt",
        "src/googlebooks/output/c.out"
    )
    solve(
        "src/googlebooks/data/d_tough_choices.txt",
        "src/googlebooks/output/d.out"
    )
    solve(
        "src/googlebooks/data/e_so_many_books.txt",
        "src/googlebooks/output/e.out"
    )
    solve(
        "src/googlebooks/data/f_libraries_of_the_world.txt",
        "src/googlebooks/output/f.out"
    )
}

fun solve(inputFile: String, outputFile: String) {
    inputReader = InputStreamReader(FileInputStream(File(inputFile)))
    val output = File(outputFile)
    output.createNewFile()
    outputWriter = OutputStreamWriter(FileOutputStream(output))
    println(inputFile)
    currLine = -1
    lines = inputReader.readLines()

    val (B, L, D) = readInts()

    val books = readInts().mapIndexed { index, score -> Book(index, score) }

    val libraries = mutableListOf<Library>()

    for (i in 0 until L) {
        val (_, T, M) = readInts()
        val libBooks = readInts().map { books[it] }
        libraries.add(Library(i, libBooks.sortedByDescending { it.score }, T, M))
    }
    libraries.forEach { library ->
        library.books.forEach { book ->
            book.numOfLibrariesWithBook++
            book.librariesWithBook.add(library.id)
            book.scoreForLibraries += library.scoreOfBooks
        }
    }

    libraries.forEach { library -> library.uniqueness += library.books.sumBy { it.numOfLibrariesWithBook } }

    val libs = solution1(books, libraries, D)
//    val libs = solution2(books, libraries, D)
//    val libs = solution3(books, libraries, D)
//    val libs = shuffleManyTimes(books, libraries, D)
    writeToOutput(libs)
    println("Score: ${calculateScore(libs)}/${books.sumBy { it.score }}")
    println("==================\n\n")
}

fun solution3(books: List<Book>, libraries: List<Library>, D: Int): List<Library> {
//    val sortedBooks = books.sortedByDescending { it.scoreForLibraries }
    val sortedBooks = books.shuffled()
    val libs = mutableListOf<Library>()
    val libsDone = mutableSetOf<Int>()
    var currDay = 0
    val booksScanned = mutableSetOf<Int>()

    for (book in sortedBooks) {
        if (!booksScanned.contains(book.id)) {
//            val bestLib = book.librariesWithBook.filter { !libsDone.contains(it) }.maxBy {
//                scoreForLib(
//                    libraries[it],
//                    currDay,
//                    D,
//                    booksScanned
//                )/libraries[it].timeToSignUp.toDouble()
//            }
//                ?: continue
            val bestLib = book.librariesWithBook.filter { !libsDone.contains(it) }.maxBy {
                    maxScoreLibCanGet(libraries[it], D)/libraries[it].timeToSignUp.toDouble()
                }
                ?: continue
            val library = libraries[bestLib]
            currDay += library.timeToSignUp
            if (currDay >= D) break

            var numOfBooksCanTake = (D - currDay) * library.maxNumBooksScan.toLong()
            var booksToTake =
                library.books.filter { _book -> !booksScanned.contains(_book.id) }.sortedByDescending { it.score }
            numOfBooksCanTake = min(booksToTake.size.toLong(), numOfBooksCanTake)
            booksToTake = booksToTake.subList(0, numOfBooksCanTake.toInt())
            library.booksToScan.addAll(booksToTake)
            booksScanned.addAll(booksToTake.map { it.id })
            libs.add(library)
            libsDone.add(library.id)
        }
    }

//    val ans = libs.filter { it.booksToScan.size > 0 }
////    println("Libraries signed up: ${ans.size}/${libraries.size}")
////    val numBooksScanned = booksScanned.size
////    println("Books scanned: ${numBooksScanned}/${books.size}")
    return libs
}

fun shuffleManyTimes(books: List<Book>, libraries: List<Library>, D: Int): List<Library> {
    var bestSol = listOf<Library>()
    var bestScore = 0

    for (i in 0 until 20) {
        val initLibs = libraries.map { it.copy() }
        val sol = solution1(books, initLibs, D)
        val score = calculateScore(sol)
        if (score > bestScore) {
            bestScore = score
            bestSol = sol
        }
    }
    return bestSol
}

fun solution1(books: List<Book>, libraries: List<Library>, D: Int): List<Library> {
//    val libs = libraries.sortedByDescending { library -> library.scoreOfBooks }
//    val libs = libraries.sortedByDescending { library -> (D - library.timeToSignUp)*library.maxNumBooksScan }
//    val libs = libraries.sortedBy { library -> library.uniqueness }
//    val libs = libraries.sortedBy { library -> library.timeToSignUp/library.scoreOfBooks }
//    val libs = libraries.sorted()
    val libs = libraries.sortedByDescending { library -> library.scoreOfBooks/library.timeToSignUp.toDouble() }
//    val libs = libraries.shuffled()
    var signUp = 0
    val booksScanned = mutableSetOf<Int>()
    for (library in libs) {
        signUp += library.timeToSignUp
        if (signUp >= D) break

        var numOfBooksCanTake = (D - signUp) * library.maxNumBooksScan.toLong()
        var booksToTake =
            library.books.filter { book -> !booksScanned.contains(book.id) }.sortedByDescending { it.score }
        numOfBooksCanTake = min(booksToTake.size.toLong(), numOfBooksCanTake)
        booksToTake = booksToTake.subList(0, numOfBooksCanTake.toInt())
        library.booksToScan.addAll(booksToTake)
        booksScanned.addAll(booksToTake.map { it.id })
    }

    val ans = libs.filter { it.booksToScan.size > 0 }
    println("Libraries signed up: ${ans.size}/${libraries.size}")
    val numBooksScanned = booksScanned.size
    println("Books scanned: ${numBooksScanned}/${books.size}")
    return ans
}

fun maxScoreLibCanGet(library: Library, D: Int): Int {
    var numOfBooksCanTake = (D - library.timeToSignUp) * library.maxNumBooksScan.toLong()
    numOfBooksCanTake = min(library.books.size.toLong(), numOfBooksCanTake)
    return library.books.sortedByDescending { it.score }.subList(0, numOfBooksCanTake.toInt()).sumBy { it.score }
}

// Not promising
fun solution2(books: List<Book>, libraries: List<Library>, D: Int): List<Library> {
    val libSet = libraries.map { it.id }.toHashSet()

    var currDay = 0
    val booksScanned = mutableSetOf<Int>()
    val libs = mutableListOf<Library>()

    while (libSet.isNotEmpty()) {
        val lib =
            chooseNextBestLibraryToSignUp(libraries.filter { libSet.contains(it.id) }, currDay, D, booksScanned)
        currDay += lib.timeToSignUp
        if (currDay >= D) break

        var numOfBooksCanTake = (D - currDay) * lib.maxNumBooksScan.toLong()
        var booksToTake =
            lib.books.filter { book -> !booksScanned.contains(book.id) }.sortedByDescending { it.score }
        numOfBooksCanTake = min(booksToTake.size.toLong(), numOfBooksCanTake)
        booksToTake = booksToTake.subList(0, numOfBooksCanTake.toInt())
        lib.booksToScan.addAll(booksToTake)
        booksScanned.addAll(booksToTake.map { it.id })
        if (lib.booksToScan.size > 0) libs.add(lib)
        libSet.remove(lib.id)
    }

    println("Libraries signed up: ${libs.size}/${libraries.size}")
    val numBooksScanned = booksScanned.size
    println("Books scanned: ${numBooksScanned}/${books.size}")
    return libs
}

fun chooseNextBestLibraryToSignUp(libraries: List<Library>, currDay: Int, D: Int, booksScanned: Set<Int>): Library {
    val bestLib = libraries.map { library -> Pair(library, scoreForLib(library, currDay, D, booksScanned)/library.timeToSignUp) }
        .maxBy { it.second }
    return bestLib!!.first
}

fun scoreForLib(library: Library, currDay: Int, D: Int, booksScanned: Set<Int>): Int {
    var numOfBooksCanTake = (D - currDay) * library.maxNumBooksScan.toLong()
    var booksToTake =
        library.books.subList(0, min(100, library.books.size)).filter { book -> !booksScanned.contains(book.id) }
            .sortedByDescending { it.score }
    numOfBooksCanTake = min(booksToTake.size.toLong(), numOfBooksCanTake)
    booksToTake = booksToTake.subList(0, numOfBooksCanTake.toInt())
    return booksToTake.sumBy { it.score }
}

fun calculateScore(libraries: List<Library>): Int {
    var score = 0
    libraries.forEach { library ->
        score += library.booksToScan.sumBy { it.score }
    }

    return score
}


fun writeToOutput(libraries: List<Library>) {
    outputWriter.appendln(libraries.size.toString())

    libraries.forEach { library ->
        outputWriter.appendln("${library.id} ${library.booksToScan.size}")
        outputWriter.appendln(library.booksToScan.map { it.id }.joinToString(" "))
    }
    outputWriter.close()
}

data class Book(val id: Int, val score: Int) {
    var numOfLibrariesWithBook = 0
    val librariesWithBook = mutableSetOf<Int>()
    var scoreForLibraries = 0
}

data class Library(val id: Int, val books: List<Book>, val timeToSignUp: Int, val maxNumBooksScan: Int) :
    Comparable<Library> {
    var booksShipped = 0
    val booksSet = setOf(books.map { it.id })
    val booksToScan = mutableListOf<Book>()
    val scoreOfBooks = books.sumBy { it.score }
    var uniqueness = 0

    override fun compareTo(other: Library): Int {
        if (other.timeToSignUp == timeToSignUp) return other.scoreOfBooks - scoreOfBooks
        return timeToSignUp - other.timeToSignUp
    }
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