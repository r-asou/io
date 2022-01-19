package rsb.io.maybe.old;

record Bytes(byte[] bytes, int length) {

	public static Bytes from(byte[] bytes, int len) {
		return new Bytes(bytes, len);
	}
}