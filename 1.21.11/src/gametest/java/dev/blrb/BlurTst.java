package dev.blrb;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class BlurTst implements FabricClientGameTest {
	ClientGameTestContext c;
	TestSingleplayerContext s;

	void ck(boolean b, String x) {
		if (!b) throw new AssertionError(x);
		System.out.println("BLRB OK: " + x);
	}

	void cfg(boolean on, int pct) {
		c.runOnClient(m -> {
			Cfg.c.on = on;
			Cfg.c.pct = pct;
		});
	}

	void tp(double x, double yaw) {
		s.getServer().runCommand("tp @p " + x + " -60 0.5 " + yaw + " 5");
	}

	void still(String n, boolean on, int pct) {
		cfg(on, pct);
		tp(0.5, 0);
		c.waitTicks(20);
		c.takeScreenshot(n);
	}

	void turn(String n, boolean on, int pct, boolean mv) {
		c.runOnClient(m -> {
			Cfg.c.on = on;
			Cfg.c.pct = pct;
			Cfg.c.mv = mv;
		});
		tp(0.5, 0);
		if (mv) Blur.tsx = 6f;
		else Blur.spin = 3f;
		c.waitTicks(12);
		c.takeScreenshot(n);
		Blur.spin = 0f;
		Blur.tsx = 0f;
	}

	void fps(String n, boolean on, int q) {
		c.runOnClient(m -> {
			Cfg.c.on = on;
			Cfg.c.pct = 100;
			Cfg.c.qual = q;
		});
		tp(0.5, 0);
		Blur.spin = 3f;
		c.waitTicks(10);
		int f0 = Blur.fr;
		long t0 = System.nanoTime();
		c.waitTicks(100);
		double sec = (System.nanoTime() - t0) / 1e9;
		System.out.println("BLRB FPS " + n + ": " + Math.round((Blur.fr - f0) / sec));
		Blur.spin = 0f;
	}

	public void runTest(ClientGameTestContext ctx) {
		c = ctx;
		try (TestSingleplayerContext sp = ctx.worldBuilder().create()) {
			s = sp;
			ctx.waitTicks(40);
			ck(Cfg.c.on && Cfg.c.pct == 50, "defaults are on at 50%");
			Cfg.c.pct = 0;
			ck(!Cfg.on(), "0% is off");
			Cfg.c = new Cfg();
			Cfg.c.pct = 250;
			Cfg.sv();
			ck(Cfg.c.pct == 100, "percent is clamped");
			Cfg.c = new Cfg();
			Cfg.c.pct = 30;
			Cfg.sv();
			Cfg.c = new Cfg();
			Cfg.ld();
			ck(Cfg.c.pct == 30, "config reloads from the json file");
			Cfg.c = new Cfg();
			Cfg.sv();
			s.getServer().runCommand("gamerule advance_time false");
			s.getServer().runCommand("time set 6000");
			String[] col = {"red", "white", "blue", "yellow", "lime", "black", "orange", "cyan"};
			for (int x = -12; x <= 12; x++) {
				s.getServer().runCommand("fill " + x + " -60 6 " + x + " -52 6 " + col[Math.floorMod(x, 8)] + "_concrete");
			}
			ctx.waitTicks(20);
			still("a_still_off", false, 100);
			still("b_still_on", true, 100);
			turn("c_turn_100", true, 100, false);
			turn("d_turn_50", true, 50, false);
			turn("e_turn_20", true, 20, false);
			turn("f_move_100", true, 100, true);
			turn("g_turn_off", false, 100, false);
			ctx.runOnClient(m -> Cfg.c = new Cfg());
			ctx.runOnClient(m -> m.setScreen(CfgScr.mk(null)));
			ctx.waitTicks(10);
			ck(ctx.computeOnClient(m -> m.screen.getClass().getSimpleName().equals("ClothConfigScreen")), "cloth config screen opens");
			ctx.takeScreenshot("i_cfg");
			ctx.runOnClient(m -> m.setScreen(null));
			ctx.runOnClient(m -> {
				Cfg.c = new Cfg();
				m.player.connection.sendCommand("motionblur 70");
			});
			ctx.waitTicks(5);
			ck(Cfg.c.pct == 70, "motionblur command sets 70");
			ctx.runOnClient(m -> m.player.connection.sendCommand("motionblur 0"));
			ctx.waitTicks(5);
			ck(!Cfg.on(), "motionblur 0 turns it off");
			ctx.runOnClient(m -> {
				m.options.enableVsync().set(false);
				m.options.framerateLimit().set(260);
			});
			ctx.waitTicks(20);
			fps("off", false, 2);
			for (int q = 0; q < 4; q++) fps("on q" + q, true, q);
			Cfg.c = new Cfg();
		}
	}
}