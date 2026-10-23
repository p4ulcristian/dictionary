# Texts are `(tr key args)`, in the language bound around them; none bound is an error

> The user's decision, 2026-10-23: "we want (tr ...)", on both sides, as Iron Rainbow writes it; not a mirror of Iron Rainbow's i18n. Changes in part [Texts are ICU messages in a flat JSON catalog per language](2026-10-23-texts-are-icu-messages-in-a-json-catalog-per-language.md): its "No dynamic language" and its "Instead of: a dynamic `*locale*`".

**Decision:**

- **A text is `(tr :orders.cart/add {:count 3})`**, on the server and in the browser; args always given, `{}` for none.
- **On the server, `tr` reads the language bound around it**: `(dictionary/with-language texts "de" …)` binds the texts and the language for everything inside. The groundwork composer binds the request's (`find-language-of-request`) around every request; an email, a PDF or a job binds the language of the row it is made for.
- **None bound is an error**: `tr` throws `:dictionary/no-language`, naming the key. Not the default language, not the fallback.
- **In the browser, `tr` is parlor's**, reading the page's language and its catalog from re-frame (dictionary has no React); the first screen is drawn once the catalog is in.
- `make-text`, with the language as a value, stays: what `tr` calls.

**Why:** the short call Iron Rainbow's pages and emails are written in; the language set once, where it is known (the request, the row), not on every line.

**Why an error when none is bound:** Iron Rainbow's `*locale*` is `"en"` unbound, so an email that forgets its binding, or a `tr` on a thread that doesn't carry it (a Java executor, a callback, a job worker; Clojure's `future`, `pmap` and agents do carry it), sends English and nothing says so. An error is seen in the first test or the first run, where it happens.

**Correction:** the decision this changes says a dynamic value is one "which a thread or a future loses": a `future` carries the bindings it was made in. A thread not made by Clojure does not.

**The price:** a job on a thread of its own binds the language again; args are written also when there are none.

**Instead of:**
- `make-text` with the language on every call, or a `t` made per language (`make-text-function`): the language written where the text is, not where it is known
- Iron Rainbow's `*locale*`, English when unbound: a wrong-language email nobody sees
- `(tr :key)` without args: an arity that fills in a value (groundwork's rules)
