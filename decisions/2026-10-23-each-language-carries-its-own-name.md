# Each language carries its own name; flags are the app's

> The user's decision, 2026-10-23. For v0.3.0. Changes in part [dictionary picks the language; texts come later](2026-10-23-dictionary-picks-the-language-texts-come-later.md) (the settings' `:languages`) and [Texts are ICU messages in a flat JSON catalog per language](2026-10-23-texts-are-icu-messages-in-a-json-catalog-per-language.md) (its settings example).

**Decision:**

- **A language in the settings is a map, its tag and its name in itself**: `:languages` and `:preview-languages` are `[{:code "en" :name "English"} {:code "de" :name "Deutsch"}]`, both keys required. `:default` and `:fallback-language` stay tags (`"en"`).
- **`list-languages-to-pick`, on both sides**, gives the languages that show here, the launched ones then the preview ones, in the settings' order, as `{:code :name}`: what a language menu draws. `list-live-languages` keeps giving the tags.
- **No flag, and nothing else about how a menu looks**: the app draws its menu, with flags if it wants them, from the tags.

**Why the name is here:** a language menu lists each language in its own name ("Deutsch", not "German"), so a person on a page in the wrong language finds theirs. Beside its tag, a name can't go missing when a language is added: `check-settings` refuses a language without one. In the app, it would be a second list of the same languages, which drifts from the settings'.

**Why the name is written, not read from ICU:** CLDR, which ICU4J and the browser's `Intl.DisplayNames` read, writes Hungarian as "magyar", lowercase; the app's menu says "Magyar". The app writes what its menu shows.

**Why no flag:** a flag is a country, not a language: English is 🇺🇸 or 🇬🇧, German 🇩🇪, 🇦🇹 or 🇨🇭. Which one, or none, is how the app's menu looks, not which language a page is in.

**The price:** the settings change shape (v0.3.0); parlor and the groundwork composer, which take dictionary's settings, move with it.

**Instead of:**
- the names left to the app (Iron Rainbow's `i18n.locales` descriptors): every app writes the list of its languages a second time
- the names from ICU / `Intl.DisplayNames`: no new setting, but a name the app can't change ("magyar")
- `{:code :name :flag}`, the flag required: every app picks flags, also one that shows none
- a separate `:language-names {"en" "English"}`: the same languages in two places in the settings
