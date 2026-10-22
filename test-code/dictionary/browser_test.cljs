(ns dictionary.browser-test
  "The browser's side, on node: the language of an address, and an
   address made in a language."
  (:require
   [cljs.test :refer [deftest is testing]]
   [dictionary.api :as dictionary]))

(def ^:private settings
  (dictionary/check-settings {:languages ["en" "de"] :default "en" :cookie-name nil}))

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

(deftest the-settings-are-checked
  (is (= :dictionary/bad-settings
         (try (dictionary/check-settings {:languages []}) nil
              (catch :default e (:type (ex-data e)))))))
