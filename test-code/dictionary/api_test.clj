(ns dictionary.api-test
  "The server's side: a request's language, by its prefix, the cookie,
   Accept-Language and the default; the alternate links; the settings
   checked."
  (:require
   [clojure.test :refer [deftest is testing]]
   [dictionary.api :as dictionary]))

(def ^:private settings
  (dictionary/check-settings {:languages ["en" "de"] :default "en" :cookie-name "lang"
                              :preview-languages ["hu"] :fallback-language "en" :catalogs-path "dictionary_test/i18n"}))

(defn- make-request [uri headers]
  {:uri uri :headers headers})

(deftest the-names-of-the-api
  (is (= '#{settings-shape check-settings find-language-of-request make-alternate-links
            split-language-from-path make-path-in-language list-live-languages
            list-catalog-files load-texts make-text find-problems-in-catalogs}
         (set (keys (ns-publics 'dictionary.api))))))

(deftest a-requests-language
  (testing "the address's prefix first, taken off the path"
    (is (= {:language "de" :path "/orders/7"}
           (dictionary/find-language-of-request settings (make-request "/de/orders/7" {"cookie" "lang=hu" "accept-language" "hu"}))))
    (is (= {:language "de" :path "/"} (dictionary/find-language-of-request settings (make-request "/de" {}))))
    (is (= {:language "en" :path "/orders"} (dictionary/find-language-of-request settings (make-request "/en/orders" {})))
        "the default's prefix, given anyway, is read as it"))
  (testing "then the cookie the settings name, when it holds one of the languages"
    (is (= "hu" (:language (dictionary/find-language-of-request settings (make-request "/orders" {"cookie" "a=1; lang=hu" "accept-language" "de"})))))
    (is (= "de" (:language (dictionary/find-language-of-request settings (make-request "/orders" {"cookie" "lang=fr" "accept-language" "de"}))))))
  (testing "then Accept-Language, most wanted first; a region falls back to its language"
    (is (= {:language "de" :path "/orders"}
           (dictionary/find-language-of-request settings (make-request "/orders" {"accept-language" "fr;q=0.9, de-AT;q=0.8, en;q=0.1"}))))
    (is (= "en" (:language (dictionary/find-language-of-request settings (make-request "/orders" {"accept-language" "de;q=0, fr"}))))
        "quality 0 is not wanted"))
  (testing "then the default"
    (is (= "en" (:language (dictionary/find-language-of-request settings (make-request "/orders" {}))))))
  (testing "a first part that isn't a language stays in the path"
    (is (= {:language "en" :path "/deals"} (dictionary/find-language-of-request settings (make-request "/deals" {})))))
  (testing "no cookie named: it isn't read"
    (is (= "en" (:language (dictionary/find-language-of-request (assoc settings :cookie-name nil)
                                                                (make-request "/orders" {"cookie" "lang=hu"}))))))
  (testing "one language: every request is in it, and no prefix is taken"
    (let [one {:languages ["hu"] :default "hu" :cookie-name nil :preview-languages [] :fallback-language "hu" :catalogs-path "i18n"}]
      (is (= {:language "hu" :path "/de/orders"}
             (dictionary/find-language-of-request one (make-request "/de/orders" {"accept-language" "de"})))))))

(deftest alternate-links
  (is (= [{:hreflang "en" :href "https://shop.example/orders"}
          {:hreflang "de" :href "https://shop.example/de/orders"}
          {:hreflang "hu" :href "https://shop.example/hu/orders"}
          {:hreflang "x-default" :href "https://shop.example/orders"}]
         (dictionary/make-alternate-links settings "https://shop.example" "/orders")))
  (is (= ["https://shop.example/" "https://shop.example/de" "https://shop.example/hu" "https://shop.example/"]
         (map :href (dictionary/make-alternate-links settings "https://shop.example" "/")))
      "the home page")
  (is (= [] (dictionary/make-alternate-links {:languages ["hu"] :default "hu" :cookie-name nil :preview-languages []
                                              :fallback-language "hu" :catalogs-path "i18n"} "https://x" "/"))
      "none with one language"))

(deftest the-settings-are-checked
  (let [errors (try (dictionary/check-settings {:languages ["en" "De"] :colour "red"})
                    (catch clojure.lang.ExceptionInfo e (:errors (ex-data e))))]
    (is (= #{:languages :default :cookie-name :preview-languages :fallback-language :catalogs-path :colour} (set (keys errors)))
        "every problem at once"))
  (let [complete {:languages ["en" "de"] :default "en" :cookie-name nil :preview-languages ["hu"]
                  :fallback-language "en" :catalogs-path "i18n"}]
    (is (= complete (dictionary/check-settings complete)))
    (is (thrown? clojure.lang.ExceptionInfo (dictionary/check-settings (assoc complete :default "hu")))
        "the default is a launched language")
    (is (thrown? clojure.lang.ExceptionInfo (dictionary/check-settings (assoc complete :fallback-language "fr")))
        "the fallback is a language that shows")
    (is (thrown? clojure.lang.ExceptionInfo (dictionary/check-settings (assoc complete :preview-languages ["de"])))
        "each language once")
    (is (thrown? clojure.lang.ExceptionInfo (dictionary/check-settings (assoc complete :catalogs-path "/i18n/"))))))

(deftest preview-languages-show-only-where-the-settings-say
  (is (= "hu" (:language (dictionary/find-language-of-request settings (make-request "/hu/orders" {}))))
      "shown here: its prefix is read")
  (let [production (assoc settings :preview-languages [] :catalogs-path "dictionary_test/i18n")]
    (is (= {:language "en" :path "/hu/orders"} (dictionary/find-language-of-request production (make-request "/hu/orders" {"accept-language" "hu"})))
        "not shown: neither its prefix nor Accept-Language")
    (is (= ["en" "de"] (map :hreflang (butlast (dictionary/make-alternate-links production "https://x" "/")))))))

(deftest texts-in-a-language
  (let [texts (dictionary/load-texts settings)]
    (is (= ["en" "de" "hu"] (map :language (dictionary/list-catalog-files settings))))
    (is (= "/dictionary_test/i18n/de.json" (:address (second (dictionary/list-catalog-files settings)))))
    (is (= "Add 3 items" (dictionary/make-text texts "en" :orders.cart/add {:count 3})))
    (is (= "1 Artikel hinzufügen" (dictionary/make-text texts "de" :orders.cart/add {:count 1})) "the language's plural rules")
    (is (= "Cart" (dictionary/make-text texts "de" :orders.cart/title {})) "missing in German: the fallback's")
    (is (= "orders.cart/nowhere" (dictionary/make-text texts "de" :orders.cart/nowhere {})) "missing everywhere: the key's name")
    (is (= "Klein (<30cm)" (dictionary/make-text texts "de" :sizes/small {})) "a < is text")
    (is (= "Kosár" (dictionary/make-text texts "hu" :orders.cart/title {})))))

(deftest a-catalog-that-isnt-there-is-named
  (is (= {:type :dictionary/missing-catalogs :resources ["dictionary_test/i18n/fr.json"]}
         (try (dictionary/load-texts (assoc settings :preview-languages ["hu" "fr"]))
              (catch clojure.lang.ExceptionInfo e (ex-data e))))))

(deftest the-catalogs-are-kept-complete
  (is (= {} (dictionary/find-problems-in-catalogs (assoc settings :languages ["en" "hu"] :preview-languages []))))
  (let [problems (dictionary/find-problems-in-catalogs {:languages ["en" "de"] :default "en" :cookie-name nil :preview-languages ["fr"]
                                                        :fallback-language "en" :catalogs-path "dictionary_test/gaps"})]
    (is (= ["a/three"] (get-in problems ["en" :missing-keys])))
    (is (= ["a/two"] (get-in problems ["de" :missing-keys])) "every key in every language")
    (is (contains? (get-in problems ["en" :bad-messages]) "a/two") "ICU can't read it")
    (is (= ["dictionary_test/gaps/fr.json"] (:missing-catalogs problems)))))
