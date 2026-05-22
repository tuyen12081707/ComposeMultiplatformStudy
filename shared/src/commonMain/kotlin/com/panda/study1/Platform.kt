package com.panda.study1

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform