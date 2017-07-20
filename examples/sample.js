"use strict"

console.log("start")

for (let i = 0; i < 10; i++) {
  setTimeout(() => {
    console.log(`alive ${i}`)
  }, 500 * i)
}

