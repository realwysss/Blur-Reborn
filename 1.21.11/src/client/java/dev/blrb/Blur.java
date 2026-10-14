package dev.blrb;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.OptionalInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class Blur {
	static final int SZ = new Std140SizeCalculator().putMat4f().putMat4f().putMat4f().putMat4f().putVec4().putVec4().get();
	static RenderPipeline pp;
	static TextureTarget tmp;
	static TextureTarget acc;
	static MappableRingBuffer ub;
	static final Matrix4f pr = new Matrix4f();
	static final Matrix4f pj = new Matrix4f();
	static double px, py, pz;
	static boolean has;
	static boolean cp;
	static long lt;
	public static float tst, tsx;
	public static boolean frz;

	static void init() {
		pp = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
				.withLocation(Identifier.fromNamespaceAndPath("blurreborn", "pipeline/motion_blur"))
				.withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
				.withFragmentShader(Identifier.fromNamespaceAndPath("blurreborn", "post/motion_blur"))
				.withSampler("InSampler")
				.withSampler("PrevSampler")
				.withSampler("DepthSampler")
				.withUniform("BlurInfo", UniformType.UNIFORM_BUFFER)
				.build();
		ub = new MappableRingBuffer(() -> "blur reborn ubo", 130, SZ);
	}

	public static void run(Matrix4f rot, Matrix4f proj, Camera cam) {
		if (!Cfg.on()) {
			has = false;
			return;
		}
		Vec3 p = cam.position();
		if (tst != 0f || tsx != 0f) {
			pr.set(rot).rotateLocalY(tst);
			pj.set(proj);
			px = p.x - tsx;
			py = p.y;
			pz = p.z;
			has = true;
			lt = System.nanoTime() - 16666667L;
		}
		long now = System.nanoTime();
		float dt = (now - lt) / 1e9f;
		lt = now;
		float dx = (float) (p.x - px), dy = (float) (p.y - py), dz = (float) (p.z - pz);
		float dd = dx * dx + dy * dy + dz * dz;
		boolean fine = has && dt > 0f && dt < 0.25f && dd < 64f;
		boolean moved = !(dd == 0f && rot.equals(pr, 1e-6f) && proj.equals(pj, 1e-6f));
		if (Cfg.c.mode == 0) {
			if (fine) draw(rot, proj, dx, dy, dz, 0f, dt);
			else seed();
		} else if (fine && moved) draw(rot, proj, dx, dy, dz, Cfg.exp() / dt, dt);
		pr.set(rot);
		pj.set(proj);
		px = p.x;
		py = p.y;
		pz = p.z;
		has = true;
	}

	static void seed() {
		if (pp == null) init();
		RenderTarget m = Minecraft.getInstance().getMainRenderTarget();
		sz(m);
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(m.getColorTexture(), acc.getColorTexture(), 0, 0, 0, 0, 0, m.width, m.height);
	}

	static void sz(RenderTarget m) {
		int w = m.width, h = m.height;
		if (tmp == null || tmp.width != w || tmp.height != h) {
			if (tmp != null) tmp.destroyBuffers();
			if (acc != null) acc.destroyBuffers();
			tmp = new TextureTarget("blur reborn", w, h, false);
			acc = new TextureTarget("blur reborn prev", w, h, false);
			cp = true;
		}
	}

	static void draw(Matrix4f rot, Matrix4f proj, float dx, float dy, float dz, float sc, float dt) {
		if (pp == null) init();
		RenderTarget m = Minecraft.getInstance().getMainRenderTarget();
		int w = m.width, h = m.height;
		cp = false;
		sz(m);
		CommandEncoder ce = RenderSystem.getDevice().createCommandEncoder();
		ce.copyTextureToTexture(m.getColorTexture(), tmp.getColorTexture(), 0, 0, 0, 0, 0, w, h);
		if (cp) {
			ce.copyTextureToTexture(m.getColorTexture(), acc.getColorTexture(), 0, 0, 0, 0, 0, w, h);
			return;
		}
		try (GpuBuffer.MappedView mv = ce.mapBuffer(ub.currentBuffer(), false, true)) {
			Std140Builder.intoBuffer(mv.data())
					.putMat4f(proj.invert(new Matrix4f()))
					.putMat4f(rot.invert(new Matrix4f()))
					.putMat4f(pr)
					.putMat4f(pj)
					.putVec4(dx, dy, dz, 0f)
					.putVec4(Math.min(sc, 8f), Cfg.cap(), Cfg.c.mode, Cfg.keep(dt));
		}
		try (RenderPass rp = ce.createRenderPass(() -> "blur reborn", m.getColorTextureView(), OptionalInt.empty())) {
			rp.setPipeline(pp);
			RenderSystem.bindDefaultUniforms(rp);
			rp.setUniform("BlurInfo", ub.currentBuffer());
			rp.bindTexture("InSampler", tmp.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			rp.bindTexture("PrevSampler", (Cfg.c.mode == 0 ? acc : tmp).getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			rp.bindTexture("DepthSampler", m.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
			rp.draw(0, 3);
		}
		ub.rotate();
		if (Cfg.c.mode == 0 && !frz) ce.copyTextureToTexture(m.getColorTexture(), acc.getColorTexture(), 0, 0, 0, 0, 0, w, h);
	}
}