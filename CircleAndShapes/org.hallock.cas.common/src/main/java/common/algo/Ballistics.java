package common.algo;

public class Ballistics {
    /**
     x = x0 + d_x n t
     y = y0 + d_y n t

     x = u0 + o_x n_2 t
     y = v0 + o_y n_2 t

     d_x^2 + d_y^2 = 1
     ===============================
     x0 + d_x n t = u0 + o_x n_2 t
     y0 + d_y n t = v0 + o_y n_2 t
     ===============================
     d_x n t - o_x n_2 t = u0 - x0
     d_y n t - o_y n_2 t = v0 - y0
     ===============================
     (d_x n - o_x n_2) t = u0 - x0
     (d_y n - o_y n_2) t = v0 - y0
     ===============================
     t = (u0 - x0) / (d_x n - o_x n_2)
     t = (v0 - y0) / (d_y n - o_y n_2)
     ===============================
     (u0 - x0) / (d_x n - o_x n_2) = (v0 - y0) / (d_y n - o_y n_2)
     ===============================
     (d_y n - o_y n_2) (u0 - x0) = (v0 - y0) (d_x n - o_x n_2)
     ===============================
     (sqrt(1 - d_x^2) n - o_y n_2) (u0 - x0) = (v0 - y0) (d_x n - o_x n_2)
     ===============================
     sqrt(1 - d_x^2) n = o_y n_2 + (v0 - y0) (d_x n - o_x n_2) / (u0 - x0)
     ===============================
     sqrt(1 - d_x^2) n = o_y n_2 + (v0 - y0) (d_x n - o_x n_2) / (u0 - x0)
     ===============================
     (1 - d_x^2) n^2 = (o_y n_2 + c (d_x n - o_x n_2))^2
     c = (v0 - y0) / (u0 - x0)
     ===============================
     (1 - d_x^2) n^2 = o_y^2 n_2^2 + 2 o_y n_2 c (d_x n - o_x n_2) + c^2 (d_x^2 n^2 - 2d_xno_xn_2 + o_x^2 n_2^2)
     c = (v0 - y0) / (u0 - x0)
     ===============================
     (1 - d_x^2) n^2 = o_y^2 n_2^2 + 2 o_y n_2 c (d_x n - o_x n_2) + c^2 (d_x^2 n^2 - 2d_xno_xn_2 + o_x^2 n_2^2)
     (-1 + d_x^2) n^2 + o_y^2 n_2^2 + 2 o_y n_2 c (d_x n - o_x n_2) + c^2 d_x^2 n^2 - 2 c^2 d_x n o_x n_2 + c^2 o_x^2 n_2^2 = 0
     (n^2 + c^2 n^2) d_x^2 + (2 o_y n_2 c n - 2 c^2 n o_x n_2) d_x + (-n^2 + o_y^2 n_2^2 - 2 o_y n_2 c o_x n_2 + c^2 o_x^2 n_2^2) = 0
     n^2(1 + c^2) d_x^2 + 2 c n n_2 (o_y - c o_x) d_x + (-n^2 + o_y^2 n_2^2 - 2 o_y n_2 c o_x n_2 + c^2 o_x^2 n_2^2) = 0
     **/

    private static final double zero_tolerance = 1e-6;

    public static Solutions getIntersections(double x0, double y0, double n, double u0, double v0, double n2, double ox, double oy) {
        Solutions solutions = new Solutions();
        if (Math.abs(u0 - x0) < zero_tolerance) {
            if (Math.abs(v0 - y0) < zero_tolerance) {
                Solution solution = new Solution();
                solution.dx = 1.0;
                solution.dy = 0.0;
                solution.ix = x0;
                solution.iy = y0;
                solution.t = 0.0;
                solutions.add(solution);
            } else {
                // have to reverse it tho...
                getIntersections(y0, x0, n, v0, u0, n2, oy, ox, true, solutions);
            }
        } else {
            getIntersections(x0, y0, n, u0, v0, n2, ox, oy, false, solutions);
        }
        return solutions;
    }

    public static void getIntersections(double x0, double y0, double n, double u0, double v0, double n2, double ox, double oy, boolean reverse, Solutions sols) {
        double k = (v0 - y0) / (u0 - x0);
        double a = n * n * (1 + k * k);
        double b = 2 * k * n * n2 * (oy - k * ox);
        double c = -n * n + oy * oy * n2 * n2 - 2 * oy * n2 * k * ox * n2 + k * k * ox * ox * n2 * n2;

        if (Math.abs(a) < zero_tolerance) {
            if (Math.abs(b) < zero_tolerance) {
                addSolutions(-c, x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
            } else {
                addSolutions(-c / b, x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
            }
        }
        double d = b * b - 4 * a * c;
        if (d < -zero_tolerance) {
//            System.out.println("no solutions");
        } else if (Math.abs(d) < 0) {
            addSolutions(-b / (2 * a), x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
        } else {
            addSolutions((-b + Math.sqrt(d)) / (2 * a), x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
            addSolutions((-b - Math.sqrt(d)) / (2 * a), x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
        }
    }

    private static void addSolutions(double dx, double x0, double y0, double n, double u0, double v0, double n2, double ox, double oy, boolean reverse, Solutions sols) {
        double y2 = 1 - dx*dx;
        if (y2 < 0) {
            return;
        }
        if (Math.abs(y2) < zero_tolerance) {
            addSolutions(dx, 0.0, x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
        } else {
            double dy = Math.sqrt(1 - dx*dx);
            addSolutions(dx, +dy, x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
            addSolutions(dx, -dy, x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
        }
    }

    private static void addSolutions(double dx, double dy, double x0, double y0, double n, double u0, double v0, double n2, double ox, double oy, boolean reverse, Solutions sols) {
        if (Math.abs(dx * n - ox * n2) > zero_tolerance) {
            double t = (u0 - x0) / (dx * n - ox * n2);
            addSolutions(t, dx, dy, x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
        } else if (Math.abs(dy * n - oy * n2) > zero_tolerance) {
            double t = (v0 - y0) / (dy * n - oy * n2);
            addSolutions(t, dx, dy, x0, y0, n, u0, v0, n2, ox, oy, reverse, sols);
        } else {
            // either the same for all t or different for all t
            // the same for all t would have been handled already in the check for starting at the same point
        }
    }

    private static void addSolutions(double t, double dx, double dy, double x0, double y0, double n, double u0, double v0, double n2, double ox, double oy, boolean reverse, Solutions sols) {
        if (t < 0)
            return;
        double x1 = x0 + dx * n * t;
        double y1 = y0 + dy * n * t;

        double x2 = u0 + ox * n2 * t;
        double y2 = v0 + oy * n2 * t;

        if (Math.abs(x1 - x2) > zero_tolerance)
            return;

        if (Math.abs(y1 - y2) > zero_tolerance)
            return;

        Solution solution = new Solution();
        if (reverse) {
            solution.dx = dy;
            solution.dy = dx;
            solution.t = t;
            solution.ix = y1;
            solution.iy = x1;
        } else {
            solution.dx = dx;
            solution.dy = dy;
            solution.t = t;
            solution.ix = x1;
            solution.iy = y1;
        }
        sols.add(solution);
    }

    public static final class Solutions {
        Solution sol1;
        Solution sol2;

        public void add(Solution solution) {
            if (sol1 == null) {sol1 = solution; return;}
            if (sol2 == null) {sol2 = solution; return;}
            throw new IllegalStateException("Can't have more than two solutions.");
        }

        public Solution minimumTimeSolution() {
            if (sol1 == null)
                return sol2;
            if (sol2 == null)
                return  sol1;
            if (sol1.t < sol2.t)
                return sol1;
            else
                return sol2;
        }
    }

    public static final class Solution {
        public double dx;
        public double dy;
        public double t;
        public double ix;
        public double iy;

        public String toString() {
            return dx + "," + dy + " @ [" + ix + "," + iy + "] when t=" + t;
        }
    }

    public static void main(String[] args) {
        Solutions solutions;

        // todo move to tests
        solutions = getIntersections(0, 0, 2, 1, 0, 1, 0, 1);
        if (solutions.sol1 != null) System.out.println(solutions.sol1);
        if (solutions.sol2 != null) System.out.println(solutions.sol2);
        System.out.println("=====================================");

        solutions = getIntersections(0, 0, 1, 1, 0, 2, 0, 1);
        if (solutions.sol1 != null) System.out.println(solutions.sol1);
        if (solutions.sol2 != null) System.out.println(solutions.sol2);
        System.out.println("=====================================");

        solutions = getIntersections(0, 0, 2, 1, 0, 1, 1, 0);
        if (solutions.sol1 != null) System.out.println(solutions.sol1);
        if (solutions.sol2 != null) System.out.println(solutions.sol2);
        System.out.println("=====================================");

        solutions = getIntersections(0, 0, 2, 0, 1, 1, 0, 1);
        if (solutions.sol1 != null) System.out.println(solutions.sol1);
        if (solutions.sol2 != null) System.out.println(solutions.sol2);
        System.out.println("=====================================");

        solutions = getIntersections(0, 0, 1, -1, 1, 1, 1, 0);
        if (solutions.sol1 != null) System.out.println(solutions.sol1);
        if (solutions.sol2 != null) System.out.println(solutions.sol2);
        System.out.println("=====================================");

        solutions = getIntersections(0, 0, 1.5, -1, 1, 1, 1, 0);
        if (solutions.sol1 != null) System.out.println(solutions.sol1);
        if (solutions.sol2 != null) System.out.println(solutions.sol2);
        System.out.println("=====================================");

        solutions = getIntersections(0, 0, 1, -2, 1, 1.5, 1, 0);
        if (solutions.sol1 != null) System.out.println(solutions.sol1);
        if (solutions.sol2 != null) System.out.println(solutions.sol2);
        System.out.println("=====================================");


        solutions = getIntersections(0, 0, 1, -2, 0, 1.5, 1, 0);
        if (solutions.sol1 != null) System.out.println(solutions.sol1);
        if (solutions.sol2 != null) System.out.println(solutions.sol2);
        System.out.println("=====================================");
    }
}
