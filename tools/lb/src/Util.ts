

class Util {
    nums(n: number): number[] {
        if (n <= 0) {
            return []
        }
        return this.nums(n - 1).concat([n - 1])
    }
}

export default new Util()