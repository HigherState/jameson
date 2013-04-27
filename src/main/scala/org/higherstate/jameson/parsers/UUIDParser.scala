package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.StringExtractor
import util.{Success, Failure, Try}
import org.higherstate.jameson.exceptions.InvalidTokenException
import java.util.UUID
import org.higherstate.jameson.{Registry, Path}

case class UUIDParser() extends StringExtractor[UUID] {
  private val uuidRegex = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".r

   protected def parse(value: String, path: Path)(implicit registry:Registry): Try[UUID] =
     if (!uuidRegex.pattern.matcher(value).matches()) Failure(InvalidTokenException("String is not a Universally Unique Identifier", path))
     else Success(UUID.fromString(value))
 }
