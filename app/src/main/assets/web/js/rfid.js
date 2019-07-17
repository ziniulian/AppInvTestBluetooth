rfid = {
	// 读
	tid: 0,
	scan: function () {
		if (!this.tid) {
			this.tid = setInterval(rfid.doScan, 100);
		}
	},
	stop: function () {
		if (this.tid) {
			clearInterval(this.tid);
			this.tid = 0;
			rfid.doScan();
		}
	},
	doScan: function () {
		var s = LwaRfdo.rfidCatchScanning();
		var o = JSON.parse(s);
		rfid.hdScan(o);
	},
	hdScan: function (obj) {
		LwaUtil.log(obj.length + " ..");
	},
	scanStart: function () {
		LwaRfdo.rfidScan();
	},
	scanStop: function () {
		LwaRfdo.rfidStop();
	},
	setBank: function (b) {
		LwaRfdo.setBank(b);
	},

	// 写
	wrt: function (bankNam, dat, tid) {
		LwaRfdo.rfidWrt(bankNam, dat, tid);
	},
	hdWrt: function (ok) {
		LwaUtil.log(ok);
	},

	// 开关
	open: function () {
		LwaRfdo.open();
	},
	close: function () {
		LwaRfdo.close();
	},

	// 蓝牙
	bleScan: function () {		// 扫描蓝牙设备
		LwaRfdo.scanDevice();
	},
	bleScanDev: function (devs) {	// 回调扫描到蓝牙设备
		LwaUtil.log(devs.length + "");
	},
	bleScanEnd: function (devs) {	// 回调蓝牙设备扫描结束
		LwaUtil.log("bleScanEnd");
	},
	getDev: function () {	// 获取上一次连接的设备
		return LwaRfdo.getDevAdr ();
	},
	setDev: function (dev) {	// 设置要连接的设备
		LwaRfdo.setDevAdr (dev);
	}
};
