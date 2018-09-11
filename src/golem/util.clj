(ns golem.util)

(defmacro run-once!
  "Runs `body`, and re-runs whenever dereferenced atoms change. If called more than once with the same `key`, the old
   reactions will be disposed, ensuring only one reaction is running at any time for the given key"
  [key & body]
  `(let [r# (golem.util/singleton-reaction ~key (fn [] ~@body))]
     (deref r#)
     r#))