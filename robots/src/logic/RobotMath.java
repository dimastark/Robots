package logic;

import java.awt.*;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.*;

public class RobotMath {
    public static double asNormalizedRadians(double angle) {
        double newAngle = angle;
        while (newAngle <= -180) newAngle += 360;
        while (newAngle > 180) newAngle -= 360;
        return newAngle;
    }

    public static double angleBetween(Robot robot, Target target) {
        double dir = robot.getDirection();
        double rx = Math.cos(dir);
        double ry = Math.sin(dir);
        double tx = target.getX() - robot.getX();
        double ty = target.getY() - robot.getY();
        double len = Math.sqrt(tx * tx + ty * ty);
        double pseudo = (rx * tx + ty * ry) / len;
        return asNormalizedRadians(Math.acos(pseudo));
    }

    public static double applyLimits(double value, double min, double max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public static class PathFinder implements Serializable {
        public HashMap<Point, ArrayList<Point>> graph;
        private Iterable<Obstacle> obstacles;
        private ArrayList<Point> points;

        public PathFinder(Iterable<Obstacle> obstacles) {
            this.obstacles = obstacles;
            points = allPoints(obstacles);
            graph = makeGraph();
        }

        public Iterable<Point> findPathTo(Point from, Point to) {
            HashMap<Point, ArrayList<Point>> tmpGraph = cloneHM(graph);
            tmpGraph.put(from, new ArrayList<>());
            tmpGraph.put(to, new ArrayList<>());
            if (!intersectsWith(obstacles, new Line2D.Double(from, to))) {
                tmpGraph.get(from).add(to);
                tmpGraph.get(to).add(from);
            }
            for (Point p : points) {
                if (!intersectsWith(obstacles, new Line2D.Double(from, p))) {
                    tmpGraph.get(from).add(p);
                    tmpGraph.get(p).add(from);
                }
                if (!intersectsWith(obstacles, new Line2D.Double(p, to))) {
                    tmpGraph.get(p).add(to);
                    tmpGraph.get(to).add(p);
                }
            }
            return Dijkstra(tmpGraph, from, to);
        }

        public static HashMap<Point, ArrayList<Point>> cloneHM(HashMap<Point, ArrayList<Point>> hm) {
            HashMap<Point, ArrayList<Point>> newHM = new HashMap<>();
            for (Point key : hm.keySet())
                newHM.put(key, new ArrayList<>());
            for (Point key : hm.keySet())
                newHM.put(key, (ArrayList<Point>)hm.get(key).clone());
            return newHM;
        }

        public static ArrayList<Point> allPoints(Iterable<Obstacle> obstacles) {
            ArrayList<Point> points = new ArrayList<>();
            for (Obstacle obst : obstacles)
                points.addAll(obst.getPivots());
            return points;
        }

        public HashMap<Point, ArrayList<Point>> makeGraph() {
            HashMap<Point, ArrayList<Point>> graph = new HashMap<>();
            points.forEach((x) -> graph.put(x, new ArrayList<>()));
            int count = points.size();
            for (int i = 0; i < count; i++)
                for (int j = i + 1; j < count; j++)
                {
                    Point fir = points.get(i);
                    Point sec = points.get(j);
                    Line2D line = new Line2D.Double(fir, sec);
                    if (!intersectsWith(obstacles, line)) {
                        graph.get(fir).add(sec);
                        graph.get(sec).add(fir);
                    }
                }
            return graph;
        }

        public HashMap<Point, ArrayList<Point>> getGraph() {
            return graph;
        }
        public static boolean intersectsWith(Iterable<Obstacle> obstacles, Line2D line) {
            for (Obstacle obst : obstacles)
                if (obst.getRectangle().intersectsLine(line))
                    return true;
            return false;
        }

        public static Iterable<Point> breadthSearch(Point start, Point to, HashMap<Point, ArrayList<Point>> graph) {
            HashSet<Point> visited = new HashSet<>();
            LinkedList<Point> queue = new LinkedList<>();
            HashMap<Point, Point> prevs = new HashMap<>();
            queue.add(start);
            while (queue.size() != 0) {
                Point node = queue.remove();
                if (visited.contains(node)) continue;
                visited.add(node);
                for (Point incidentNode : graph.get(node)) {
                    if (incidentNode.equals(to)) {
                        LinkedList<Point> path = new LinkedList<>();
                        prevs.put(to, node);
                        Point prev = to;
                        while (prev != null) {
                            path.addFirst(prev);
                            prev = prevs.get(prev);
                        }
                        return path;
                    }
                    if (!visited.contains(node))
                        prevs.put(incidentNode, node);
                    queue.add(incidentNode);
                }
            }
            return null;
        }

        public static Iterable<Point> Dijkstra(HashMap<Point, ArrayList<Point>> graph, Point start, Point end) {
            ArrayList<Point> notVisited = new ArrayList<>(graph.keySet());
            HashMap<Point, DijkstraData> track = new HashMap<>();
            HashMap<Line2D, Double> lens = new HashMap<>();
            track.put(start, new DijkstraData(null, 0));
            while (true) {
                Point toOpen = null;
                double bestPrice = Double.POSITIVE_INFINITY;
                for (Point e : notVisited) {
                    if (track.containsKey(e) && track.get(e).price < bestPrice) {
                        bestPrice = track.get(e).price;
                        toOpen = e;
                    }
                }
                if (toOpen == null) return null;
                if (toOpen == end) break;
                for (Point e : graph.get(toOpen)) {
                    Line2D line = new Line2D.Double(toOpen, e);
                    double len = 0;
                    if (lens.containsKey(line))
                        len = lens.get(line);
                    else {
                        double xPart = (line.getX1() - line.getX2());
                        double yPart = (line.getY1() - line.getY2());
                        len = xPart * xPart + yPart * yPart;
                        lens.put(line, len);
                    }
                    double currentPrice = track.get(toOpen).price + len;
                    if (!track.containsKey(e) || track.get(e).price > currentPrice)
                        track.put(e, new DijkstraData(toOpen, currentPrice));
                }
                notVisited.remove(toOpen);
            }
            LinkedList<Point> result = new LinkedList<>();
            while (end != null) {
                result.addFirst(end);
                end = track.get(end).prev;
            }
            return result;
        }
    }

    static class DijkstraData {
        public Point prev;
        public double price;

        public DijkstraData(Point prev, double price) {
            this.prev = prev;
            this.price = price;
        }
    }
}
