package ch.smart.code.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * 如果此可为空的字符序列不为“null”或不为空，则返回“true”。
 */
@UseExperimental(ExperimentalContracts::class)
fun CharSequence?.isNotNullOrEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrEmpty != null)
    }
    return !this.isNullOrEmpty()
}


/**
 * 如果此可为空的字符序列不为“null”或空，或者不由空白字符组成，则返回“true”。
 */
@UseExperimental(ExperimentalContracts::class)
fun CharSequence?.isNotNullOrBlank(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrBlank != null)
    }
    return !this.isNullOrBlank()
}

