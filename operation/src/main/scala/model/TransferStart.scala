package model

/*
transferId (type: Int): Represents the unique identifier of the transfer.
sourceId (type: Int): Represents the identifier of the source.
destinationId (type: Int): Represents the identifier of the destination.
value (type: Int): Represents the value associated with the transfer.
category (type: Option[String]): Represents an optional category associated with the transfer. The category is represented as an Option type, which means it can either be Some(category) where category is a String value, or None if no category is specified.
 */
case class TransferStart (transferId: Int, sourceId: Int, destinationId: Int, value: Int, category: Option[String])