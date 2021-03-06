;;; Copyright (c) Care Logistics, inc. All rights reserved.
;;; The use and distribution terms for this software are covered by the
;;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;; which can be found in the file epl-v10.html at the root of this
;;; distribution.
;;; By using this software in any fashion, you are agreeing to be bound by
;;; the terms of this license.
;;; You must not remove this notice, or any other, from this software.

(ns somni
  (:require [schema.core :as s]
            [somni.http.errors :refer [server-error
                                       not-found]]
            [somni.middleware.exceptions :refer [wrap-uncaught-exceptions]]
            [somni.router :as router]
            [somni.stacker :as stacker]))

(defn add-prefix
  [resources prefix]
  (if prefix
    (map #(update-in % [:uri] (partial str prefix "/")) resources)
    resources))

(defn build
  [resources deps &
   {:keys [on-missing on-error uri-prefix dev-mode]
    :or {on-error  #(server-error % dev-mode)
         on-missing not-found}}]

  {:pre [(seq resources)
         (map? deps)]}

  (let [resources (add-prefix resources uri-prefix)
        stack-fn #(stacker/stack % deps on-error)
        stacked   (mapcat stack-fn resources)
        router    (router/add-routes {} stacked)
        handler   (router/router->handler router)]

    handler))
