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

	void turn(String n, boolean on, int pct) {
		cfg(on, pct);
		tp(0.5, 0);
		Blur.tst = 0.05f;
		c.waitTicks(10);
		c.takeScreenshot(n);
		Blur.tst = 0f;
	}

	void mode(int m) {
		c.runOnClient(x -> Cfg.c.mode = m);
	}

	void snap(String n) {
		tp(0.5, 0);
		c.waitTicks(15);
		Blur.frz = true;
		tp(0.5, 14);
		c.waitTicks(2);
		c.takeScreenshot(n);
		Blur.frz = false;
	}

	void strafe(String n, boolean on, int pct) {
		cfg(on, pct);
		tp(0.5, 0);
		Blur.tsx = 0.25f;
		c.waitTicks(10);
		c.takeScreenshot(n);
		Blur.tsx = 0f;
	}
	public void runTest(ClientGameTestContext ctx) {
		c = ctx;
		try (TestSingleplayerContext sp = ctx.worldBuilder().create()) {
			s = sp;
			ctx.waitTicks(40);
			ck(Cfg.c.on && Cfg.c.pct == 50 && Cfg.c.mode == 1, "defaults are on at 50%");
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
			for (int m = 0; m < 3; m++) {
				mode(m);
				still("m" + m + "_still", true, 100);
				if (m == 0) {
					cfg(true, 100);
					snap("m0_turn_100");
					cfg(true, 50);
					snap("m0_turn_50");
				} else {
					turn("m" + m + "_turn_100", true, 100);
					turn("m" + m + "_turn_50", true, 50);
					strafe("m" + m + "_strafe_100", true, 100);
				}
			}
			turn("z_turn_off", false, 100);
			ctx.runOnClient(m -> Cfg.c = new Cfg());
			ctx.runOnClient(m -> m.setScreen(CfgScr.mk(null)));
			ctx.waitTicks(10);
			ck(ctx.computeOnClient(m -> m.screen.getClass().getSimpleName().equals("ClothConfigScreen")), "cloth config screen opens");
			ctx.takeScreenshot("i_cfg");
			ctx.runOnClient(m -> m.setScreen(null));
			cfg(true, 100);
			long t0 = System.nanoTime();
			ctx.waitTicks(100);
			long on = System.nanoTime() - t0;
			cfg(false, 100);
			t0 = System.nanoTime();
			ctx.waitTicks(100);
			long off = System.nanoTime() - t0;
			System.out.println("BLRB TIME: 100 ticks on=" + on / 1000000 + "ms off=" + off / 1000000 + "ms");
		}
	}
}