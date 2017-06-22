package org.walleth.ui

import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.View
import kotlinx.android.synthetic.main.transaction_item.view.*
import org.ligi.kaxt.setVisibility
import org.threeten.bp.ZoneOffset
import org.walleth.activities.TransactionActivity.Companion.getTransactionActivityIntentForHash
import org.walleth.data.addressbook.AddressBook
import org.walleth.data.exchangerate.ETH_TOKEN
import org.walleth.data.exchangerate.TokenProvider
import org.walleth.data.transactions.Transaction
import org.walleth.data.transactions.getTokenTransferTo
import org.walleth.data.transactions.getTokenTransferValue
import org.walleth.data.transactions.isTokenTransfer
import org.walleth.functions.resolveNameFromAddressBook

class TransactionViewHolder(itemView: View, val direction: TransactionAdapterDirection) : RecyclerView.ViewHolder(itemView) {


    fun bind(transaction: Transaction, addressBook: AddressBook, tokenProvider: TokenProvider) {

        val relevantAddress = if (direction == TransactionAdapterDirection.INCOMMING) {
            transaction.from
        } else {
            transaction.to
        }

        if (transaction.isTokenTransfer()) {
            itemView.address.text = transaction.getTokenTransferTo().resolveNameFromAddressBook(addressBook)
            val firstOrNull = tokenProvider.getAllTokens().firstOrNull { it.address == relevantAddress.hex }
            if (firstOrNull != null) {
                itemView.difference.setValue(transaction.getTokenTransferValue(), firstOrNull)
            }
        } else {
            itemView.difference.setValue(transaction.value, ETH_TOKEN)
            itemView.address.text = relevantAddress.resolveNameFromAddressBook(addressBook)
        }

        itemView.transaction_err.setVisibility(transaction.error != null)
        if (transaction.error != null) {
            itemView.transaction_err.text = transaction.error
        }

        val localTime = transaction.localTime
        val epochMillis = localTime.toEpochSecond(ZoneOffset.systemDefault().rules.getOffset(localTime)) * 1000
        val context = itemView.context
        itemView.date.text = DateUtils.getRelativeDateTimeString(context, epochMillis,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                0
        )

        itemView.isClickable = true
        itemView.setOnClickListener {
            transaction.txHash?.let {
                context.startActivity(context.getTransactionActivityIntentForHash(it))
            }

        }
    }

}