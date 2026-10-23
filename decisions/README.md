# dictionary decisions

One file per decision: `YYYY-MM-DD-what-we-decided.md`. The title is the decision. Each file says what we decided, why, and what we chose against. A decision changes by a new file; the old one says so at the top (`Replaced by` or `Changed in part by`), and nothing else in it is rewritten.

dictionary is groundwork's languages (groundwork: the libraries and the composer of the same name that puts them together). It carries over the languages of Groundwork v0.5.0, now the groundwork composer (its decisions are in its `decisions/`), and the texts of Iron Rainbow's i18n.

## In force

- [dictionary picks the language; texts come later](2026-10-23-dictionary-picks-the-language-texts-come-later.md), changed in part by [Texts are ICU messages in a flat JSON catalog per language](2026-10-23-texts-are-icu-messages-in-a-json-catalog-per-language.md) and [Each language carries its own name; flags are the app's](2026-10-23-each-language-carries-its-own-name.md)
- [A request's language: the prefix, the cookie, Accept-Language, the default; the default's addresses carry no prefix](2026-10-23-a-requests-language-and-the-defaults-addresses.md), changed in part by [Texts are ICU messages in a flat JSON catalog per language](2026-10-23-texts-are-icu-messages-in-a-json-catalog-per-language.md)
- [Texts are ICU messages in a flat JSON catalog per language, read by both sides](2026-10-23-texts-are-icu-messages-in-a-json-catalog-per-language.md), changed in part by [Each language carries its own name; flags are the app's](2026-10-23-each-language-carries-its-own-name.md) and [Texts are `(tr key args)`, in the language bound around them; none bound is an error](2026-10-23-texts-are-tr-under-a-bound-language.md)
- [Each language carries its own name; flags are the app's](2026-10-23-each-language-carries-its-own-name.md)
- [Texts are `(tr key args)`, in the language bound around them; none bound is an error](2026-10-23-texts-are-tr-under-a-bound-language.md)
