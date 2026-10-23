> **Changed in part** by [Texts are `(tr key args)`, in the language bound around them; none bound is an error](2026-10-23-texts-are-tr-under-a-bound-language.md) (2026-10-23): a text is `(tr key args)` in the language bound around it (`with-language`); none bound is an error. Its "No dynamic language" and "Instead of: a dynamic `*locale*`" are replaced.

> **Changed in part** by [Each language carries its own name; flags are the app's](2026-10-23-each-language-carries-its-own-name.md) (2026-10-23, v0.3.0): a language in `:languages` and `:preview-languages` is `{:code :name}`; the settings example's tags are maps now.

# Texts are ICU messages in a flat JSON catalog per language, read by both sides

> **Changed in part** by [The family and its composer are groundwork](https://github.com/p4ulcristian/groundwork/blob/Starting-from-the-bottom/decisions/2026-10-23-the-family-and-its-composer-are-groundwork.md) (2026-10-23): manor, the family, and house, the composer, are both named groundwork now.

> The user's decision, 2026-10-23: carried over from Iron Rainbow's i18n (its `i18n.core`, `i18n.locale`, `i18n.locales` and its catalog test), adapted to manor's rules. Since v0.2.0. Changes in part [dictionary picks the language; texts come later](2026-10-23-dictionary-picks-the-language-texts-come-later.md) (the texts are here now) and [A request's language](2026-10-23-a-requests-language-and-the-defaults-addresses.md) (the languages read are the ones that show).

**Decision:**

- **A message is an ICU MessageFormat string**, in a flat JSON catalog per language, `<catalogs-path>/<language>.json`. The same file is a classpath resource for the server (ICU4J: emails, PDFs, text the server writes) and a static file the browser fetches (intl-messageformat), at `/<catalogs-path>/<language>.json` (the app's server serves it; house does).
- **Keys are namespaced keywords**: `:orders.cart/add` is the JSON's `"orders.cart/add"`.
- **A key missing from a language falls back to the fallback language's message; missing there too, it shows as the key's name**, `"orders.cart/add"`, ugly on purpose, so it is seen.
- **A test helper keeps the catalogs complete**: `find-problems-in-catalogs` gives every key some catalog has and another lacks, every message ICU can't read, and every catalog that isn't there; an app's test says `(is (= {} …))`.
- **Languages are launched or in preview**: `:languages` are launched and show everywhere; `:preview-languages` are the ones not launched that show in this environment. A language that shows is read from an address's prefix, the cookie and Accept-Language, gets alternate links, and has a catalog; one that doesn't is none of these.
- **The browser re-renders when the catalog swaps through parlor**, the re-frame layer (parlor's decision): dictionary's browser side is plain functions, `fetch-catalog!` and `make-text`.
- **API**: server `load-texts` (every catalog that shows, once, at the start), `make-text`, `list-catalog-files`, `find-problems-in-catalogs`; browser `fetch-catalog!`, `make-text`; both `list-live-languages`.

**Adapted to manor's rules:**

1. **The fallback language and the catalogs' path are required settings**, `:fallback-language` and `:catalogs-path`; no `"en"` and no `public/i18n` written in the code. The fallback is a language that shows; it may differ from `:default` (whose addresses carry no prefix).
2. **Which preview languages show is a required setting**, `:preview-languages`, `[]` where none show (production); not a test of an environment variable inside the library (Iron Rainbow's `IRONRAINBOW_DEV`, which once hid them from the stage where they were to be reviewed).
3. **No dynamic language for emails and PDFs**: `make-text` takes the language as a value, `(make-text texts "de" :mail/subject {:order 7})`, the language of the row the email or PDF is made for, handed down as logbook hands down its context. Iron Rainbow's `*locale*`, bound per request, is not carried over.

**A person's lasting choice of language is the app's:** Iron Rainbow keeps it in a user column (for emails and PDFs, made without a request) and writes the cookie when the person picks one; dictionary only reads the cookie (`:cookie-name`) and never writes either.

```clojure
{:languages ["en" "de"] :default "en" :cookie-name "lang"
 :preview-languages []          ; ["hu"] on a stage
 :fallback-language "en"
 :catalogs-path     "i18n"}     ; i18n/en.json, i18n/de.json on the classpath; /i18n/de.json for the browser
```

**What stays fixed, and why:** the file name `<language>.json` and the flat map of text keys: both ends and the translators' tools read it. `ignoreTag` in the browser: a `<` in a message is text, as ICU4J reads it on the server; without it intl-messageformat reads a tag and throws.

**Why:** one file per language, read by both sides, means a message is translated once for the screen, the email and the PDF. ICU messages carry plurals and genders a translator can write without code. Ugly key names and a test that wants every key everywhere catch a missing translation before a person does.

**The price:** two message libraries, ICU4J on the server (a large jar) and intl-messageformat in the browser (the app's package.json); their grammars differ at the edges (tags), which `ignoreTag` closes. The catalogs are read once, at the start: a changed catalog shows after a restart, in development too.

**Instead of:**
- the catalogs in the page: every page would carry every message, and the browser couldn't keep a language's catalog between pages
- a reagent atom in dictionary for the browser (Iron Rainbow's): dictionary would need React; parlor already holds what screens draw from
- a dynamic `*locale*`: a value nobody passed, which a thread or a future loses
