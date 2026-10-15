package dev.blrb;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.ArrayDeque;
import java.util.OptionalInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class Blur {
	record S(long t, Quaternionf q, double x, double y, double z) {
	}

	static final int SZ = new Std140SizeCalculator().putMat4f().putMat4f().putMat4f().putMat4f().putVec4().putVec4().putVec4().get();
	static final long T0 = System.nanoTime();
	static final ArrayDeque<S> hs = new ArrayDeque<>();
	static RenderPipeline pp;
	static RenderPipeline pm;
	static TextureTarget half;
	static MappableRingBuffer ub;
	public static float spin, tsx;
	public static int fr;

	static void init() {
		pp = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
				.withLocation(Identifier.fromNamespaceAndPath("blurreborn", "pipeline/motion_blur"))
				.withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
				.withFragmentShader(Identifier.fromNamespaceAndPath("blurreborn", "post/motion_blur"))
				.withSampler("InSampler")
				.withSampler("DepthSampler")
				.withUniform("BlurInfo", UniformType.UNIFORM_BUFFER)
				.build();
		pm = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
				.withLocation(Identifier.fromNamespaceAndPath("blurreborn", "pipeline/motion_mix"))
				.withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
				.withFragmentShader(Identifier.fromNamespaceAndPath("blurreborn", "post/motion_mix"))
				.withSampler("HalfSampler")
				.withBlend(BlendFunction.TRANSLUCENT)
				.build();
		ub = new MappableRingBuffer(() -> "blur reborn ubo", 130, SZ);
	}

	static float ang(Quaternionf a, Quaternionf b) {
		Quaternionf q = new Quaternionf(a).mul(new Quaternionf(b).conjugate());
		return 2f * (float) Math.sqrt((double) q.x * q.x + (double) q.y * q.y + (double) q.z * q.z);
	}

	public static void run(Matrix4f rot, Matrix4f proj, Camera cam) {
		fr++;
		if (!Cfg.on()) {
			hs.clear();
			return;
		}
		long now = System.nanoTime();
		double sec = (now - T0) / 1e9;
		Matrix4f cur = spin != 0f ? new Matrix4f(rot).rotateLocalY((float) (spin * sec)) : rot;
		Vec3 p = cam.position();
		S s = new S(now, new Quaternionf().setFromNormalized(cur), p.x + tsx * sec, p.y, p.z);
		S last = hs.peekLast();
		if (last != null) {
			double ddx = s.x - last.x, ddy = s.y - last.y, ddz = s.z - last.z;
			if (now - last.t > 250000000L || ddx * ddx + ddy * ddy + ddz * ddz > 64) hs.clear();
		}
		hs.addLast(s);
		while (hs.size() > 2 && now - hs.peekFirst().t > 150000000L) hs.removeFirst();
		long tt = now - (long) (Cfg.exp() * 1e9f);
		S a = null, b = null;
		for (S o : hs) {
			if (o.t <= tt) a = o;
			else {
				b = o;
				break;
			}
		}
		if (a == null) a = hs.peekFirst();
		if (b == null || a == s) return;
		float f = b.t == a.t ? 0f : Math.max(0f, Math.min(1f, (float) (tt - a.t) / (b.t - a.t)));
		Quaternionf qo = new Quaternionf(a.q).slerp(b.q, f);
		double ox = a.x + (b.x - a.x) * f, oy = a.y + (b.y - a.y) * f, oz = a.z + (b.z - a.z) * f;
		float dx = (float) (s.x - ox), dy = (float) (s.y - oy), dz = (float) (s.z - oz);
		RenderTarget m = Minecraft.getInstance().getMainRenderTarget();
		float px = ang(s.q, qo) * proj.m11() * m.height * 0.5f;
		boolean mv = Cfg.c.mv && dx * dx + dy * dy + dz * dz > 1e-8f;
		if (px < 0.75f && !mv) return;
		draw(m, cur, proj, new Matrix4f().rotation(qo), mv ? dx : 0f, mv ? dy : 0f, mv ? dz : 0f);
	}

	static void draw(RenderTarget m, Matrix4f rot, Matrix4f proj, Matrix4f pr, float dx, float dy, float dz) {
		if (pp == null) init();
		int w = Math.max(1, m.width / 2), h = Math.max(1, m.height / 2);
		if (half == null || half.width != w || half.height != h) {
			if (half != null) half.destroyBuffers();
			half = new TextureTarget("blur reborn half", w, h, false);
		}
		CommandEncoder ce = RenderSystem.getDevice().createCommandEncoder();
		try (GpuBuffer.MappedView mv = ce.mapBuffer(ub.currentBuffer(), false, true)) {
			Std140Builder.intoBuffer(mv.data())
					.putMat4f(proj.invert(new Matrix4f()))
					.putMat4f(rot.invert(new Matrix4f()))
					.putMat4f(pr)
					.putMat4f(proj)
					.putVec4(dx, dy, dz, 0f)
					.putVec4(1f, Cfg.cap(), Cfg.c.mv ? 1f : 0f, 0f)
					.putVec4(Cfg.smp(), Cfg.c.ctr / 100f, m.height, (float) m.width / m.height);
		}
		try (RenderPass rp = ce.createRenderPass(() -> "blur reborn", half.getColorTextureView(), OptionalInt.of(0))) {
			rp.setPipeline(pp);
			RenderSystem.bindDefaultUniforms(rp);
			rp.setUniform("BlurInfo", ub.currentBuffer());
			rp.bindTexture("InSampler", m.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			rp.bindTexture("DepthSampler", m.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			rp.draw(0, 3);
		}
		ub.rotate();
		try (RenderPass rp = ce.createRenderPass(() -> "blur reborn mix", m.getColorTextureView(), OptionalInt.empty())) {
			rp.setPipeline(pm);
			RenderSystem.bindDefaultUniforms(rp);
			rp.bindTexture("HalfSampler", half.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			rp.draw(0, 3);
		}
	}
}
