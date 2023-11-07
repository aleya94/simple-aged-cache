package io.collective


class CachedNode(var key: Any?, var value: Any?, val expiry: Long?) {
    var next: CachedNode? = null;
}