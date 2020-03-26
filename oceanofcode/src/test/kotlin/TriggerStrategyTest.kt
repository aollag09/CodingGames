import org.junit.jupiter.api.Test

internal class TriggerStrategyTest {

  @Test
  fun test_trigger_map_1() {
    val env = EnvTest.generateEnvTest1()
    env.trackerKasakta.candidates.clear()
    env.trackerKasakta.candidates.addAll(listOf(Vector2D(6, 3), Vector2D(5, 3)))
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.terrible.mines.add(Vector2D(6, 6))
    env.terrible.position = Vector2D()
    env.trackerKasakta.testPrintMap(false)

    val trigger = TriggerStrategy(env.terrible, env.trackerKasakta).apply()
    assert(trigger is Trigger)
    assert((trigger as Trigger).target == Vector2D(6, 6))
  }

  @Test
  fun test_trigger_map_1_one_candidate() {
    val env = EnvTest.generateEnvTest1()
    env.trackerKasakta.candidates.clear()
    env.trackerKasakta.candidates.addAll(listOf(Vector2D(6, 2)))
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.terrible.mines.add(Vector2D(6, 6))
    env.terrible.position = Vector2D()
    env.trackerKasakta.testPrintMap(false)

    val trigger = TriggerStrategy(env.terrible, env.trackerKasakta).apply()
    assert(trigger is Trigger)
    assert((trigger as Trigger).target == Vector2D(6, 6))
  }

  @Test
  fun test_trigger_map_1_next_to_mine() {
    val env = EnvTest.generateEnvTest1()
    env.trackerKasakta.candidates.clear()
    env.trackerKasakta.candidates.addAll(listOf(Vector2D(6, 2), Vector2D(5, 2)))
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.terrible.mines.add(Vector2D(6, 6))
    env.terrible.position = Vector2D()
    env.trackerKasakta.testPrintMap(false)

    val trigger = TriggerStrategy(env.terrible, env.trackerKasakta).apply()
    assert(trigger is Trigger)
    assert((trigger as Trigger).target == Vector2D(6, 6))
  }

  @Test
  fun test_trigger_map_1_half_candidates() {
    val env = EnvTest.generateEnvTest1()
    env.trackerKasakta.candidates.clear()
    env.trackerKasakta.candidates.addAll(listOf(Vector2D(10, 2), Vector2D(5, 2)))
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.terrible.mines.add(Vector2D(6, 6))
    env.terrible.position = Vector2D()
    env.trackerKasakta.testPrintMap(false)

    val trigger = TriggerStrategy(env.terrible, env.trackerKasakta).apply()
    assert(trigger is Trigger)
    assert((trigger as Trigger).target == Vector2D(6, 6))
  }

  @Test
  fun test_trigger_map_1_half_candidates_next() {
    val env = EnvTest.generateEnvTest1()
    env.trackerKasakta.candidates.clear()
    env.trackerKasakta.candidates.addAll(listOf(Vector2D(10, 2), Vector2D(6, 2)))
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.terrible.mines.add(Vector2D(6, 6))
    env.terrible.position = Vector2D()
    env.trackerKasakta.testPrintMap(false)

    val trigger = TriggerStrategy(env.terrible, env.trackerKasakta).apply()
    assert(trigger is Trigger)
    assert((trigger as Trigger).target == Vector2D(6, 6))
  }

  @Test
  fun test_trigger_map_1_not_trigger() {
    val env = EnvTest.generateEnvTest1()
    env.trackerKasakta.candidates.clear()
    env.trackerKasakta.candidates.addAll(listOf(Vector2D(10, 2), Vector2D(9, 2)))
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.terrible.mines.add(Vector2D(6, 6))
    env.terrible.position = Vector2D()
    env.trackerKasakta.testPrintMap(false)

    val trigger = TriggerStrategy(env.terrible, env.trackerKasakta).apply()
    assert(trigger is Empty)
  }


  @Test
  fun test_trigger_map_1_third_candidates() {
    val env = EnvTest.generateEnvTest1()
    env.trackerKasakta.candidates.clear()
    env.trackerKasakta.candidates.addAll(listOf(Vector2D(10, 2), Vector2D(9, 2), Vector2D(5, 2)))
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.trackerKasakta.trail.add(Direction.S)
    env.terrible.mines.add(Vector2D(6, 6))
    env.terrible.position = Vector2D()
    env.trackerKasakta.testPrintMap(false)
    val trigger = TriggerStrategy(env.terrible, env.trackerKasakta).apply()
    assert(trigger is Empty)
  }

}