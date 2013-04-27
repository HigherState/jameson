package org.higherstate.jameson

import org.higherstate.jameson.parsers._
import org.higherstate.jameson.extractors._
import reflect.runtime.universe._

object Dsl {

  case class RequiredSelector[U,T](key:U, parser:Parser[T], replaceKey:Option[U]) extends Selector[U, T] {
    def isRequired = true
  }

  implicit class StringTupleExtenstion(val self:(String, String)) extends AnyVal {
    def ->>[T](parser:Parser[T]) = RequiredSelector(self._1, parser, Some(self._2))
  }
  implicit class AnyExt[U](val self:U) extends AnyVal {
    def ->>[T](parser:Parser[T]) = RequiredSelector(self, parser, None)
  }


  implicit class UnrequiredSelectorWithReplaceKey[T](val self:((String, String),Parser[T])) extends AnyVal with Selector[String, T] {
    def key = self._1._1
    def parser = self._2
    def isRequired = false
    def replaceKey = Some(self._1._2)
  }
  implicit class UnrequiredSelector[U, T](val self:(U,Parser[T])) extends AnyVal with Selector[U, T] {
    def key = self._1
    def parser = self._2
    def isRequired = false
    def replaceKey = None
  }

  object || extends ListParserLike {
    def apply[T](implicit registry:Registry, typeTag:TypeTag[T]) = NestedListParser[T](registry[T])
    def apply[T](parser:Parser[T])(implicit registry:Registry, typeTag:TypeTag[T]) = NestedListParser[T](parser)
  }

  object #* extends MapParserLike {
    def apply(selectors:Selector[String, _]*) = OpenMapParser(selectors.map(s => s.key -> s).toMap)
  }

  object ¦¦ extends TraversableOnceParserLike {
    def apply[T](implicit registry:Registry, typeTag:TypeTag[T])  = NestedTraversableOnceParser[T](registry[T])
    def apply[T](parser:Parser[T])(implicit registry:Registry, typeTag:TypeTag[T]) = NestedTraversableOnceParser(parser)
  }

  def #!(selector:Selector[String, _], selectors:Selector[String, _]*) =
    CloseMapParser((selectors :+ selector).map(s => s.key -> s).toMap)

  def #^(selector:Selector[String, _], selectors:Selector[String, _]*) =
    DropMapParser((selectors :+ selector).map(s => s.key -> s).toMap)

  object ? extends DefaultOptionParser {
    def apply[U](parser:Parser[U]) = OptionParser(parser)
    def apply[U](parser:Parser[U], orElse:U) = OrElseParser(parser, orElse)
  }

  def ><[T,U](leftParser:Extractor[_, T], rightParser:Extractor[_, U]) = EitherParser(leftParser,rightParser)

  //maybe defaults should be extractors...
  def /[T, U](key:String, selectors:Selector[T, U]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
    MatchParser(key, true, registry[T].asInstanceOf[Extractor[_, T]], selectors.map(p => p.key -> p.parser.asInstanceOf[KeyValuePairsExtractor[U]]).toMap)

  def /[U](key:String, classes:ClassParser[_]*)(implicit registry:Registry) =
    MatchParser(key, true, registry[String].asInstanceOf[Extractor[_, String]], classes.map(p => p.getClassName -> p.asInstanceOf[KeyValuePairsExtractor[U]]).toMap)

  def >>[T <: AnyRef](implicit registry:Registry, typeTag:TypeTag[T]) = ClassParser[T](Nil, registry)

  def >>[T <: AnyRef](selectors:Selector[String, _]*)(implicit registry:Registry, typeTag:TypeTag[T]) = {
    ClassParser[T](selectors.toList, registry)
  }

  object AsAny extends AnyParser
  object AsBool extends BooleanParser
  object AsByte extends ByteParser
  object AsChar extends CharParser
  object AsDouble extends DoubleParser
  object AsFloat extends FloatParser
  object AsInt extends IntParser
  object AsLong extends LongParser
  object AsNull extends NullParser
  object AsShort extends ShortParser
  object AsString extends StringParser
  object AsUUID extends UUIDParser
}
