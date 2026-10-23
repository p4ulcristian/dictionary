> **Changed in part** by [The family and its composer are groundwork](https://github.com/p4ulcristian/groundwork/blob/Starting-from-the-bottom/decisions/2026-10-23-the-family-and-its-composer-are-groundwork.md) (2026-10-23): manor, the family, and house, the composer, are both named groundwork now.

> **Changed in part** by [Texts are ICU messages in a flat JSON catalog per language, read by both sides](2026-10-23-texts-are-icu-messages-in-a-json-catalog-per-language.md) (2026-10-23, v0.2.0): the texts are here: ICU messages in a JSON catalog per language, read by both sides.

# dictionary picks the language; texts come later

> The user's decision, 2026-10-23. Carries over Groundwork v0.5.0's `groundwork.web.languages` (house's tag v0.5.0) and its browser routing's languages.

**Decision:** dictionary is a library of its own in manor, on both sides, for one idea: which language a request, a page and an address are in. It picks the request's language on the server, gives the page's `<html lang>` and its alternate links (hreflang), and in the browser reads the language from an address and makes an address in a language. The texts of a page in that language (translations) are not in it yet: they come later, in this library, when they are decided.

- **Server:** `find-language-of-request` (the language, and the path without its prefix), `make-alternate-links`.
- **Browser:** `find-language-of-path`.
- **Both:** `settings-shape`, `check-settings`, `split-language-from-path`, `make-path-in-language`.
- **Its settings**, every key required: `{:languages ["en" "de"] :default "en" :cookie-name "lang"}`, `:cookie-name` nil for none.
- **Needs only malli.** It knows no router, no page shell and no library of manor: mailroom's routing, parlor and house call it.

**Why the name:** the user chose dictionary, over phrasebook, interpreter and languages: the book a page looks its words up in, which is where the texts will live too.

**Why its own library:** the language is read where a request comes in (house, the server), where an address is followed (parlor and mailroom, the browser) and where the page is written (house); each needs the same rules (which prefix, which default), and none of them is about languages.

**Instead of:**
- the languages inside mailroom's routing: mailroom would know the languages, and house's page shell would need mailroom to write `<html lang>`
- waiting for translations to make the library: the routing and the page shell need the language now; texts are a second question
