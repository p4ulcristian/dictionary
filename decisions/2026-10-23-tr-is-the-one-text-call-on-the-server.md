# tr is the one text call on the server

> The user's decision, 2026-10-23, from the tidy-up. Built on tidy-up, not released.

**Decision:** an app gets a text on the server only with `(tr key args)`, in the language bound around the request (`with-language`, which the composer binds for every request). `make-text` leaves the public api and stays dictionary's inside, the function `tr` calls.

```clojure
(dictionary/tr :orders.cart/add {:count 3})   ; the request's language
```

**Why:** one way to get a text; the language is never passed by hand, so it can't be the wrong one.

**The price:** code outside a request (a job writing an email) binds the language itself with `with-language`, the person's language read first.

**Instead of:** keeping `make-text` public beside `tr`: two calls for one thing.

## Settled while building

Choices made while building, not asked:

1. **Only the server's `make-text` leaves the api.** The browser's stays public: parlor's `tr` calls it (parlor is another library), and the app calls `parlor/tr`. Its docstring says so.
2. **`make-text` stays in `dictionary.catalogs`, now private,** called only by `make-bound-text` (tr's). `tr`'s docstring now says what `make-text`'s said: the args' names, the fallback, the key's name when no catalog has it.
3. **The tests read every text through `with-language` and `tr`;** the names-of-the-api test no longer lists `make-text`.

Left for later, outside dictionary (no caller in bridge or parlor): the composer's `test-code/groundwork/server/main/api_test.clj` (two `dictionary/make-text` checks) and `source-code/groundwork/server/main/README.md` (names `dictionary/make-text`); Iron Rainbow's `project/code/i18n/core.cljc` (server half, on a released dictionary) and its `GROUNDWORK-1.0.md`.
