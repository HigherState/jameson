package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import util.Try


//case class FlattenParser[U,T <: Traversable[U], V <: Traversable[T]](parser:Parser[V]) extends Parser[T] {
//  def parse(tokenizer: Tokenizer, path: Path): Try[U] = {
//    val p = parser.parse(tokenizer, path)
//    p.map(_.flatten)
//  }
//
//}
