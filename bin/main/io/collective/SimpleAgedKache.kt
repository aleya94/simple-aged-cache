package io.collective

import java.time.Clock


class SimpleAgedKache {

    //difference between var and val
    //var is mutable variable
    //val is read-only variable
    //? means the variable can be nullable or set to null
    private var currSize: Int = 0
    private var headNode: CachedNode? = null
    private var myCacheClock: Clock? = null
    private val expiryObject: ExpirableEntry = ExpirableEntry()

    constructor(clock: Clock?) {
        currSize = 0
        headNode = null
        myCacheClock = clock
    }

    constructor() {
        currSize = 0
        headNode = null
        myCacheClock = Clock.systemDefaultZone();
    }

    inner class ExpirableEntry {

        //there is recursive and non-recursive way to do this, doing it in non-recursive way
        fun pruneExpiredEntries() {
            var currNode: CachedNode? = headNode
            var prevNode: CachedNode? = null

            while (currNode != null) {
                if (currNode.expiry!! < myCacheClock?.millis()!!) {
                    //Initial condition, if head node is expired and prev node is null, set head node to next node
                    if (prevNode == null) {
                        headNode = currNode.next
                        currSize--
                    } else {
                        //Not initial condition, remove the currNode by "skipping" over it (by connecting prevNode.next to next node aka currNode.next
                        prevNode.next = currNode.next
                        currSize--
                    }
                } else {
                    //if the currNode is not expired, set the currNode to be prevNode and continue processing the nextNode as currNode
                    prevNode = currNode
                }

                //Must progress through each node and prune them as necessary
                currNode = currNode.next
            }

        }

        /*fun pruneExpiredEntriesAlternateImplementation() {
            if (currSize == 0) {
                return
            } else if (currSize == 1) {
                if ((headNode?.expiry ?: Long.MAX_VALUE) < myCacheClock?.millis()!!) {
                    headNode = null
                    currSize--
                }
            } else if (currSize > 1) {
                var iterateNode: CachedNode? = headNode

                while (iterateNode?.next != null) {
                    //if the next node is expired, be prepared to erase it from the list
                    if ((iterateNode.next?.expiry ?: Long.MAX_VALUE) < myCacheClock?.millis()!!) {
                        //if the next next is an actual node
                        if (iterateNode.next!!.next != null) {
                            iterateNode.next = iterateNode.next!!.next
                            currSize--
                        } else {
                            //if the next next is null or end of list
                            iterateNode.next = null
                            currSize--
                            break // Exit the loop when the last node is removed
                        }
                    } else {
                        // Progress to the next node
                        iterateNode = iterateNode.next
                    }
                }
            }
        }*/

        fun printCache() {
            print("Cache content with size ${currSize} is: [")
            if (currSize == 0) {
                print("]")
            } else if (currSize == 1) {
                val nodeKey = headNode?.key
                val nodeValue = headNode?.value
                val nodeExpiry = headNode?.expiry
                print("{${nodeKey}: [${nodeValue}, ${nodeExpiry}]}")
                print("]")
            } else if (currSize > 1) {
                var iterateNode = headNode
                for (i in 0 until currSize) {
                    val nodeKey = iterateNode?.key
                    val nodeValue = iterateNode?.value
                    val nodeExpiry = iterateNode?.expiry
                    print("{${nodeKey}: [${nodeValue}, ${nodeExpiry}]}")

                    if (iterateNode != null) {
                        if (iterateNode.next != null) {
                            print(", ")
                        }
                    }

                    if (iterateNode != null) {
                        iterateNode = iterateNode.next
                    }

                }

                print("]")
            }
            println()
        }
    }

    fun put(key: Any?, value: Any?, retentionInMillis: Int) {

        //if the key already exists, do nothing
        if (isIn(key)) {
            return
        }
        expiryObject.pruneExpiredEntries()
        if (currSize == 0) {
            //must initialize a new linked list data structure with a head node
            //to store the initial key, value, and expiryTime data
            headNode = CachedNode(key, value, myCacheClock?.millis()?.plus(retentionInMillis.toLong()))
            currSize++
        } else if (currSize > 0) {
            //add to front of the linked list to avoid iterating to the end of the linked list
            val tempNode: CachedNode = CachedNode(key, value, myCacheClock?.millis()?.plus(retentionInMillis.toLong()))
            //safe call expression
            tempNode.next = headNode
            headNode = tempNode
            currSize++
        }

    }

    fun isEmpty(): Boolean {
        var result = false

        if (currSize <= 0) {
            result = true
        }

        return result
    }

    fun size(): Int {
        expiryObject.pruneExpiredEntries()
        return currSize
    }

    fun get(key: Any?): Any? {

        expiryObject.pruneExpiredEntries()

        var iterateNode = headNode
        if (iterateNode != null) {
            while (iterateNode?.next != null) {
                if (iterateNode.key == key) {
                    return iterateNode.value
                }
                iterateNode = iterateNode.next
            }

            //completed the iteration, check for the last node to confirm
            if (iterateNode != null) {
                if (iterateNode.key == key) {
                    return iterateNode.value
                }
            }
        }

        return null
    }

    fun isIn(key: Any?): Boolean {
        var result = false
        var iterateNode = headNode
        if (iterateNode != null) {
            while (iterateNode?.next != null) {
                if (iterateNode.key == key) {
                    result = true
                    return result
                }
                iterateNode = iterateNode.next
            }

            //completed the iteration, check for the last node to confirm
            if (iterateNode != null) {
                if (iterateNode.key == key) {
                    result = true
                    return result
                }
            }
        }
        return result
    }
}