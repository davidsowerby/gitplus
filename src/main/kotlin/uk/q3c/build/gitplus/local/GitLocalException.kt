package uk.q3c.build.gitplus.local

class GitLocalException : RuntimeException {


    constructor(message: String) : super(message) {
    }

    constructor(message: String, e: Exception) : super(message, e) {
    }
}
