# tr is the one text call on the server

> The user's decision, 2026-10-23, from the tidy-up. Not built yet.

**Decision:** an app gets a text on the server only with `(tr key args)`, in the language bound around the request (`with-language`, which the composer binds for every request). `make-text` leaves the public api and stays dictionary's inside, the function `tr` calls.

```clojure
(dictionary/tr :orders.cart/add {:count 3})   ; the request's language
```

**Why:** one way to get a text; the language is never passed by hand, so it can't be the wrong one.

**The price:** code outside a request (a job writing an email) binds the language itself with `with-language`, the person's language read first.

**Instead of:** keeping `make-text` public beside `tr`: two calls for one thing.
