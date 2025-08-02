package com.wordonline.server.game.domain.object.component.mob.pathfinder;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.Vector2;
import java.util.*;

public class AstarPathFinder implements PathFinder {
    private final int width, height;
    private final float cellSize;
    private final boolean[][] walkable;

    public AstarPathFinder(int width, int height, float cellSize) {
        this.width     = width;
        this.height    = height;
        this.cellSize  = cellSize;
        this.walkable  = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Vector2 cellCenter = new Vector2(
                        x * cellSize + cellSize/2f,
                        y * cellSize + cellSize/2f
                );
                walkable[x][y] = GameConfig.OBSTACLES.stream()
                        .noneMatch(o -> cellCenter.distance(o.center) < o.radius);
            }
        }
    }

    @Override
    public List<Vector2> findPath(Vector2 startPos, Vector2 endPos) {
        Node[][] nodes = new Node[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y] = new Node(x, y, walkable[x][y]);
            }
        }

        Node startNode = worldToNode(startPos, nodes);
        Node endNode   = worldToNode(endPos,   nodes);

        // A*
        PriorityQueue<Node> openSet   = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Set<Node>           closedSet = new HashSet<>();
        startNode.g = 0;
        startNode.h = heuristic(startNode, endNode);
        startNode.f = startNode.h;
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.equals(endNode)) {
                return buildPath(current);
            }

            closedSet.add(current);

            for (Node neighbor : getNeighbors(current, nodes)) {
                if (!neighbor.walkable || closedSet.contains(neighbor)) continue;

                double tentativeG = current.g + distance(current, neighbor);
                if (tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g      = tentativeG;
                    neighbor.h      = heuristic(neighbor, endNode);
                    neighbor.f      = neighbor.g + neighbor.h;
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private Node worldToNode(Vector2 pos, Node[][] nodes) {
        int cx = (int)(pos.getX() / cellSize);
        int cy = (int)(pos.getY() / cellSize);
        cx = Math.max(0, Math.min(cx, width  - 1));
        cy = Math.max(0, Math.min(cy, height - 1));
        return nodes[cx][cy];
    }

    private List<Vector2> buildPath(Node end) {
        List<Vector2> path = new ArrayList<>();
        for (Node cur = end; cur != null; cur = cur.parent) {
            float wx = cur.x * cellSize + cellSize * 0.5f;
            float wy = cur.y * cellSize + cellSize * 0.5f;
            path.add(new Vector2(wx, wy));
        }
        Collections.reverse(path);
        return path;
    }

    private double heuristic(Node a, Node b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    private double distance(Node a, Node b) {
        int dx = Math.abs(a.x - b.x), dy = Math.abs(a.y - b.y);
        return (dx == 1 && dy == 1) ? Math.sqrt(2) : 1.0;
    }

    private List<Node> getNeighbors(Node node, Node[][] nodes) {
        List<Node> neighbors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = node.x + dx, ny = node.y + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    neighbors.add(nodes[nx][ny]);
                }
            }
        }
        return neighbors;
    }

    // A* 내부에서 쓰일 노드
    private static class Node {
        final int x, y;
        final boolean walkable;
        double g = Double.MAX_VALUE, h, f;
        Node parent = null;
        Node(int x, int y, boolean walkable) { this.x = x; this.y = y; this.walkable = walkable; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Node)) return false;
            Node n = (Node)o;
            return this.x == n.x && this.y == n.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
    }
}
