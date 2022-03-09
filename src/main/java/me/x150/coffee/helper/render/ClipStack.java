package me.x150.coffee.helper.render;

import me.x150.coffee.mixin.MatrixStackAccessor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;

import java.util.Deque;
import java.util.Stack;

public class ClipStack {
    public static final ClipStack globalInstance = new ClipStack();
    final Stack<TransformationEntry> clipStack = new Stack<>();

    //Rectangle lastStack = null;
    public void addWindow(MatrixStack stack, Rectangle r1) {
        Matrix4f matrix = stack.peek().getPositionMatrix();
        Vector4f coord = new Vector4f((float) r1.getX(), (float) r1.getY(), 0, 1);
        Vector4f end = new Vector4f((float) r1.getX1(), (float) r1.getY1(), 0, 1);
        coord.transform(matrix);
        end.transform(matrix);
        double x = coord.getX();
        double y = coord.getY();
        double endX = end.getX();
        double endY = end.getY();
        Rectangle r = new Rectangle(x, y, endX, endY);
        if (clipStack.empty()) {
            clipStack.push(new TransformationEntry(r, stack.peek()));
//            Renderer.R2D.renderLine(stack,Color.RED,r.getX(),r.getY(),r.getX1(),r.getY1());

            Renderer.R2D.beginScissor(r.getX(), r.getY(), r.getX1(), r.getY1());
        } else {
            Rectangle lastClip = clipStack.peek().rect;
            double lsx = lastClip.getX();
            double lsy = lastClip.getY();
            double lstx = lastClip.getX1();
            double lsty = lastClip.getY1();
            double nsx = MathHelper.clamp(r.getX(), lsx, lstx);
            double nsy = MathHelper.clamp(r.getY(), lsy, lsty);
            double nstx = MathHelper.clamp(r.getX1(), nsx, lstx);
            double nsty = MathHelper.clamp(r.getY1(), nsy, lsty); // totally intended varname
            clipStack.push(new TransformationEntry(new Rectangle(nsx, nsy, nstx, nsty), stack.peek()));
//            Renderer.R2D.renderLine(stack,Color.RED,nsx,nsy,nstx,nsty);
            Renderer.R2D.beginScissor(nsx, nsy, nstx, nsty);
        }
    }

    public void popWindow() {

        clipStack.pop();
        if (clipStack.empty()) {
            Renderer.R2D.endScissor();
        } else {
            TransformationEntry r1 = clipStack.peek();
            Rectangle r = r1.rect;
//            Renderer.R2D.renderLine(stack,Color.BLUE,r.getX1(),r.getY(),r.getX(),r.getY1());
            MatrixStack s = new MatrixStack();
            Deque<MatrixStack.Entry> p = ((MatrixStackAccessor) s).getStack();
            p.clear();
            p.add(r1.transformationEntry);
            Renderer.R2D.beginScissor(r.getX(), r.getY(), r.getX1(), r.getY1());

        }
    }

    public void renderOutsideClipStack(Runnable e) {
        if (clipStack.empty()) {
            e.run();
        } else {
            Renderer.R2D.endScissor();
            e.run();
            Rectangle r = clipStack.peek().rect;
            Renderer.R2D.beginScissor(r.getX(), r.getY(), r.getX1(), r.getY1());
        }
    }

    record TransformationEntry(Rectangle rect, MatrixStack.Entry transformationEntry) {
    }
}