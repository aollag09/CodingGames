class EnvTest {

  companion object {
    fun generateEnvTest1(): Env {
      return Env(MapTest.generateMapTest1());
    }

    fun generateEnvTest2(): Env {
      return Env(MapTest.generateMapTest2());
    }

    fun generateEnvSmallEmptyTest(): Env{
      return Env(MapTest.generateSmallEmptyMapTest());
    }
  }

}
