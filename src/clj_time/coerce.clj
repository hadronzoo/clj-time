(ns clj-time.coerce
  "Utilites to coerce Joda DateTime instances to and from various other types.
   For example, to convert a Joda DateTime to and from a Java long:

     => (to-long (date-time 1998 4 25))
     893462400000

     => (from-long 893462400000)
     #<DateTime 1998-04-25T00:00:00.000Z>"
  (:refer-clojure :exclude [extend])
  (:use clj-time.core)
  (:require [clj-time.format :as time-fmt])
  (:import (org.joda.time DateTime DateTimeZone))
  (:import java.util.Date java.sql.Timestamp))

(defprotocol ICoerce
  (to-long [obj]
    "Convert `obj` to the number of milliseconds after the Unix epoch.")
  (to-date [obj]
    "Convert `obj` to a Java Date instance.")
  (to-date-time [obj]
    "Convert `obj` to a Joda DateTime instance.")
  (to-string [obj]
    "Returns a string representation of obj in UTC time-zone
    using (ISODateTimeFormat/dateTime) date-time representation.")
  (to-timestamp [obj]
    "Convert `obj` to a Java SQL Timestamp instance."))

(defn from-long
  "Returns a DateTime instance in the UTC time zone corresponding to the given
   number of milliseconds after the Unix epoch."
  [#^Long millis]
  (DateTime. millis #^DateTimeZone utc))

(defn from-string
  "return DateTime instance from string using
   formatters in clj-time.format, returning first
   which parses"
  [s]
  (first
   (for [f (vals time-fmt/formatters)
         :let [d (try (time-fmt/parse f s) (catch Exception _ nil))]
         :when d] d)))

(defn from-date
  "Returns a DateTime instance in the UTC time zone corresponding to the given
   Java Date object."
  [#^Date date]
  (from-long (.getTime date)))

(extend-type nil
  ICoerce
  (to-date [_] nil)
  (to-date-time [_] nil)
  (to-long [_] nil)
  (to-string [_] nil)
  (to-timestamp [_] nil))

(extend-type Date
  ICoerce
  (to-date [date]
    date)
  (to-date-time [date]
    (DateTime. (to-long date) utc))
  (to-long [date]
    (.getTime date))
  (to-string [date]
    (to-string (to-date-time date)))
  (to-timestamp [date]
    (Timestamp. (.getTime date))))

(extend-type DateTime
  ICoerce
  (to-date [dt]
    (Date. (to-long dt)))
  (to-date-time [dt]
    dt)
  (to-long [dt]
    (.getMillis dt))
  (to-string [dt]
    (time-fmt/unparse (:date-time time-fmt/formatters) dt))
  (to-timestamp [dt]
    (Timestamp. (.getMillis dt))))

(extend-type Integer
  ICoerce
  (to-date [number]
    (Date. (long number)))
  (to-date-time [number]
    (DateTime. (long number) utc))
  (to-long [number]
    (long number))
  (to-string [number]
    (to-string (to-date-time number)))
  (to-timestamp [number]
    (Timestamp. (long number))))

(extend-type Long
  ICoerce
  (to-date [number]
    (Date. number))
  (to-date-time [number]
    (DateTime. number utc))
  (to-long [number]
    (Long. number))
  (to-string [number]
    (to-string (to-date-time number)))
  (to-timestamp [number]
    (Timestamp. (long number))))

(extend-type String
  ICoerce
  (to-date [s]
    (to-date (to-date-time s)))
  (to-date-time [s]
    (from-string s))
  (to-long [s]
    (to-long (to-date-time s)))
  (to-string [s]
    (to-string (to-date-time s)))
  (to-timestamp [s]
    (to-timestamp (to-date-time s))))

(extend-type Timestamp
  ICoerce
  (to-date [ts]
    (Date. (to-long ts)))
  (to-date-time [ts]
    (DateTime. (to-long ts) utc))
  (to-long [ts]
    (.getTime ts))
  (to-string [ts]
    (to-string (to-date-time ts)))
  (to-timestamp [ts]
    ts))
