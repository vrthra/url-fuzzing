import saarland.cispa.se.tribble.dsl._

//following 4.3 URL writing at https://url.spec.whatwg.org/
//including parts from https://tools.ietf.org/html/rfc3986#appendix-A and //https://tools.ietf.org/html/rfc6874
//(whenever the living standard documentation was not sufficient to formulate a grammar)

Grammar(
  'url := "" ~ (('absoluteURL ~'relativeURLwithFragment) | 'absoluteURLwithFragment),
  'absoluteURLwithFragment :=('absoluteURL ~ ("#" ~ 'URLfragment).?).?,
  'absoluteURL := (('URLspecialSchemeNotFile ~ ":" ~ 'schemeRelativeSpecialURL)
    | ('URLnonSpecialScheme ~ ":" ~ 'relativeURL)
    | ('URLschemeFile ~ ":" ~ 'schemeRelativeFileURL)) ~ ("?" ~ 'URLquery).?,

  'URLspecialSchemeNotFile := "ftp" | "http" | "https" | "ws" | "wss", 
  'URLnonSpecialScheme := 'alpha ~ ('alphanum | "+" | "-" | ".").rep,
  'URLschemeFile := "file",

  'relativeURLwithFragment := ('relativeURL ~ ("#" ~ 'URLfragment).?).?, 
  'relativeURL := ('specialSchemeNotFile | 'fileScheme | 'otherScheme) ~ ("?" ~ 'URLquery).?,
  'specialSchemeNotFile := 'schemeRelativeSpecialURL | 'pathAbsoluteURL | 'pathRelativeSchemelessURL,// | 'ws,
  'fileScheme := 'schemeRelativeFileURL | 'pathAbsoluteURL
    | 'pathAbsoluteNonWindowsFileURL | 'pathRelativeSchemelessURL,// | 'empty,// | 'ws,
  'otherScheme := 'schemeRelativeURL | 'pathAbsoluteURL | 'pathRelativeSchemelessURL,
  'schemeRelativeSpecialURL := "//" ~ 'host ~ (":" ~ 'URLport ~ 'pathAbsoluteURL.?).?, //removed empty alternative

  
  'schemeRelativeURL := "//" ~ 'opaqueHostAndPort ~ 'pathAbsoluteURL.?, //removed empty alternative
  'opaqueHostAndPort := 'opaqueHost ~ (":" ~ 'URLport).?,
  'opaqueHost := 'URLunit.rep | ("[" ~ 'ipv6address ~ "]"),
  'schemeRelativeFileURL := "//" ~ (('host ~ 'pathAbsoluteNonWindowsFileURL.?) | 'pathAbsoluteURL ),//removed empty alternative
  'pathAbsoluteURL := "/" ~ 'pathRelativeURL,
  'pathAbsoluteNonWindowsFileURL := 'pathAbsoluteURL ~ 'windowsDriveLetter ~ "/", 
  'windowsDriveLetter := 'alpha ~ (":" | "|"),
  'pathRelativeURL := 'URLpathSegment ~ ("/" ~ 'pathRelativeURL).?,
  'pathRelativeSchemelessURL := ('pathRelativeURL ~ ":").?,
  //pathRelativeURL can't start with URLscheme
  'URLpathSegment := ('URLunit.rep) | 'singleDotPathSegment | 'doubleDotPathSegment,
  // URLunit can't be /,?, singleDotPathSegment, doubleDotPathSegment
  'singleDotPathSegment := "." | "%2e",
  'doubleDotPathSegment := ".." | ".%2e" | "%2e." | "%2e%2e",
  'URLquery := 'URLunit.rep,
  'URLfragment := 'URLunit.rep,
   // 0<=port<=65535
  'URLport := ('digit.rep(1,4))		
		| (( "1" | "2" | "3" | "4"| "5") ~ 'digit.rep(4))
		| ("6" ~ ("0" | "1" | "2" | "3" | "4") ~ 'digit.rep(3))
		| ("65" ~ ("0" | "1" | "2" | "3" | "4") ~ 'digit.rep(2))
		| ("655" ~ ("0" | "1" | "2" ) ~ 'digit)
		| ("6553" ~ ( "0"|"1" | "2" | "3" | "4"| "5")),
  'URLunit := 'URLcodePoint | 'percentEncodedByte,
  'URLcodePoint := 'unreserved, //| 'unicode |'reserved ,
  //'reserved := ":" | "/" | "?" | "#" | "[" | "]" | "@" | 'subdelims,
  'subdelims := "!" | "$" | "&" | "'" | "(" | ")" | "*" | "+" | "," | ";" | "=",
  'unreserved := 'alphanum | "-" | "." | "_" | "~",
  //unicode: code points in u+00A0 to u+10FFFD, excluding surrogates(u+D800-u+DFFF) and noncharachters(u+FDD0-u+FDEF)
  //u+00A0 - u+0FFF
  //u+1000-u+CFFF, u+E000-u+EFFF
  //u+10000-u+10EFFF
  //u+10F000-u+10FEFF
  //u+10FF00-u+10FFEF
  //u+10FFF0-u+10FFFD
  //u+D000-u+D7FF
  //u+F000-u+FCFF, u+FE00-u+FFFF
  //u+FD00 - u+FDCF
  //u+FDF0-u+FDFF
  //'unicode := "U+" ~ (("0" ~ (("0" ~ "A" ~ 'unicodeHEX) | (("[1-9]".regex | "[A-F]".regex) ~ 'unicodeHEX.rep(2, 2))))
  //  | (("[1-9]".regex | "A" | "B" | "C" | "E") ~ 'unicodeHEX.rep(3, 3))
  //  | ("10" ~ ((('digit | "[A-E]".regex) ~ 'unicodeHEX.rep(3, 3))
  //  | ("F" ~ ('digit | "[A-E]".regex) ~ 'unicodeHEX.rep(2, 2))
  //  | ("FF" ~ ('digit | "[A-E]".regex) ~ 'unicodeHEX)
  //  | ("FFF" ~ ('digit | "[A-D]".regex))))
  //  | ("D" ~ "[0-7]".regex ~ 'unicodeHEX.rep(2, 2))
  //  | ("F" ~ ('digit | "A" | "B" | "C" | "E" | "F") ~ 'unicodeHEX.rep(2, 2))
  //  | ("FD" ~ ('digit | "A" | "B" | "C") ~ 'unicodeHEX)
  //  | ("FDF" ~ 'unicodeHEX)),
  'host := ('userinfo ~ "@").? ~ 'domain,
  'domain := ('unreserved | 'subdelims ).rep  | 'ipv4address | ("[" ~ 'ipv6address ~ "]"), // removed percent encoded //TODO forbidden host code points
  'userinfo := ('unreserved | 'percentEncodedByte | 'subdelims | ":").rep,
  'ipv4address := 'decoctet ~ "." ~ 'decoctet ~ "." ~ 'decoctet ~ "." ~ 'decoctet,
  'ipv6address := (('h16 ~ ":").rep(6, 6) ~ 'ls32)
    | ((('h16 ~ ":").rep(0, 1) ~ 'h16).? ~ "::" ~ ('h16 ~ ":").rep(2, 2) ~ 'ls32)
    | ((('h16 ~ ":").rep(0, 2) ~ 'h16).? ~ "::" ~ 'h16 ~ ":" ~ 'ls32)
    | ((('h16 ~ ":").rep(0, 3) ~ 'h16).? ~ "::" ~ 'ls32)
    | ((('h16 ~ ":").rep(0, 4) ~ 'h16).? ~ "::" ~ 'h16)
    | ((('h16 ~ ":").rep(0, 5) ~ 'h16).? ~ "::"),
  'decoctet := 'digit | ("[1-9]".regex ~ 'digit)
    | ("1" ~ 'digit.rep(2, 2))
    | ("2" ~ ("0" | "1" | "2" | "3" | "4") ~ 'digit)
    | ("25" ~ ("0" | "1" | "2" | "3" | "4" | "5")),
  'h16 := 'hexdig ~ 'hexdig ~ 'hexdig ~ 'hexdig,
  'ls32 := ('h16 ~ ":" ~ 'h16) | 'ipv4address,
  'digit := "[0-9]".regex,
  'alphanum := "[a-zA-Z0-9]".regex,
  'alpha := "[a-zA-Z]".regex,
  'hexdig := ("[a-f]".regex) | 'digit,
  //'unicodeHEX := 'digit | ("[A-F]".regex),
  'percentEncodedByte := "%" ~ 'hexdig ~ 'hexdig,
  //'ws := " " | "\\t" | "\\r" | "\\n" , //TODO make sure the final tests contain \n etc
  //'empty := ""
)

