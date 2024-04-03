package com.zhpew.findapp


/**
 * 两种情况：
 * 1、匹配，next[i] = next[i-1]
 * 2、不匹配，找相同前后缀的更小相同前后缀
 *  例如： ABACABAB next数组为 [0,0,1,0,1,2,3,?]
 *  此时 array[index] = B , 而 array[cursor] = C ,不匹配
 *  但是，next[next[index-1]] = 1 ，有更小前后缀
 *  所以可以根据 array[next[next[index-1]]] = array[index]
 *  所以 next[index] = next[next[index-1]]+1 = 2
 *  cursor = next[next[index-1]]+1 = 2
 *
 *  如果出现next[next[index-1]] = 0 或 next[index-1] = 0 的情况，则相同前后缀没有更小的相同前后缀了，所以从头匹配即可
 */
fun getNext(key: String): IntArray {
    // 全部初始化为0
    val next = IntArray(key.length) { 0 }
    var cursor = 0
    for (index in 1 until key.length) {
        if (key[index] == key[cursor]) {
            next[index] = next[index - 1] + 1
            cursor++
        } else {
            // 找更小的前后缀
            val lastSame = next[index - 1]
            if (lastSame == 0 || next[lastSame - 1] == 0) {
                // 没有更小前后缀了，从头开始匹配
                if (key[0] == key[index]) {
                    next[index] = 1
                    cursor = 1
                } else {
                    next[index] = 0
                    cursor = 0
                }
            } else {
                if (key[index] == key[next[lastSame - 1]]) {
                    // 有更小前后缀与更小前后缀匹配了
                    next[index] = next[lastSame - 1] + 1
                    cursor = lastSame
                } else {
                    // 没匹配上更小前后缀，从头开始匹配了
                    if (key[0] == key[index]) {
                        next[index] = 1
                        cursor = 1
                    } else {
                        next[index] = 0
                        cursor = 0
                    }
                }
            }
        }
    }
    return next
}

fun isMatch(target: String, key: String, next: IntArray): Boolean {
    var i = 0
    var j = 0
    while (i < target.length) {
        if (target[i] == key[j]) {
            i++
            j++
        } else if (j > 0) {
            j = next[j - 1]
        } else {
            // 第一个字符就匹配失败了
            i++
        }
        if (j == key.length) {
            // 匹配成功
            return true
        }
    }
    return false
}

