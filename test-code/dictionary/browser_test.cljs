(ns dictionary.browser-test
  "The browser's side, on node: the language of an address, and an
   address made in a language."
  (:require
   [cljs.test :refer [async deftest is testing]]
   [dictionary.api :as dictionary]))

(def ^:private settings
  (dictionary/check-settings {:languages [{:code "en" :name "English"} {:code "de" :name "Deutsch"}] :default "en" :cookie-name nil
                              :preview-languages [] :fallback-language "en" :catalogs-path "i18n"}))

(deftest the-language-of-an-address
  (is (= "de" (dictionary/find-language-of-path settings "/de/orders" "en")))
  (testing "no prefix: the language the page came in"
    (is (= "de" (dictionary/find-language-of-path settings "/orders" "de")))
    (is (= "en" (dictionary/find-language-of-path settings "/orders" "en"))))
  (is (= {:language "de" :path "/orders/7"} (dictionary/split-language-from-path settings "/de/orders/7"))))

(deftest an-address-in-a-language
  (is (= "/de/orders" (dictionary/make-path-in-language settings "/orders" "de")))
  (is (= "/orders" (dictionary/make-path-in-language settings "/orders" "en")) "the default carries none")
  (is (= "/de" (dictionary/make-path-in-language settings "/" "de")) "the home page"))

(deftest the-languages-a-menu-lists
  (is (= [{:code "en" :name "English"} {:code "de" :name "Deutsch"}] (dictionary/list-languages-to-pick settings))))

(deftest the-settings-are-checked
  (is (= :dictionary/bad-settings
         (try (dictionary/check-settings {:languages []}) nil
              (catch :default e (:type (ex-data e)))))))

(def ^:private texts
  {:fallback-language "en"
   :catalogs          {"en" {"orders.cart/add" "Add {count, plural, one {# item} other {# items}}" "orders.cart/title" "Cart"
                             "sizes/small" "Small (<30cm)"}
                       "de" {"orders.cart/add" "{count, plural, one {# Artikel} other {# Artikel}} hinzufügen"}}})

(deftest texts-in-a-language
  (is (= "Add 3 items" (dictionary/make-text texts "en" :orders.cart/add {:count 3})))
  (is (= "1 Artikel hinzufügen" (dictionary/make-text texts "de" :orders.cart/add {:count 1})))
  (is (= "Cart" (dictionary/make-text texts "de" :orders.cart/title {})) "missing in German: the fallback's")
  (is (= "orders.cart/nowhere" (dictionary/make-text texts "de" :orders.cart/nowhere {})) "missing everywhere: the key's name")
  (is (= "Small (<30cm)" (dictionary/make-text texts "en" :sizes/small {})) "a < is text, as on the server"))

(deftest a-catalog-is-fetched-from-the-same-file
  (async done
         (let [fetch  (.-fetch js/globalThis)
               asked  (atom nil)]
           (set! (.-fetch js/globalThis) (fn [address _] (reset! asked address)
                                           (js/Promise.resolve #js {:ok true :json (fn [] (js/Promise.resolve #js {"a/one" "Eins"}))})))
           (-> (dictionary/fetch-catalog! settings "de")
               (.then (fn [catalog]
                        (is (= "/i18n/de.json" @asked))
                        (is (= {"a/one" "Eins"} catalog))))
               (.finally (fn [] (set! (.-fetch js/globalThis) fetch) (done)))))))
