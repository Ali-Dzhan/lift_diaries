package app.config;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.exercise.model.Exercise;
import app.exercise.repository.ExerciseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                categoryRepository.saveAll(List.of(
                        Category.builder().name("Chest").build(),
                        Category.builder().name("Back").build(),
                        Category.builder().name("Legs").build(),
                        Category.builder().name("Shoulders").build(),
                        Category.builder().name("Arms").build(),
                        Category.builder().name("Abdomen").build()
                ));
            }
        };
    }

    @Bean
    CommandLineRunner initExercises(ExerciseRepository exerciseRepository, CategoryRepository categoryRepository) {
        return args -> {
            if (exerciseRepository.count() == 0) {

                Category abdomen = categoryRepository.findByName("Abdomen");
                Category arms = categoryRepository.findByName("Arms");
                Category back = categoryRepository.findByName("Back");
                Category chest = categoryRepository.findByName("Chest");
                Category legs = categoryRepository.findByName("Legs");
                Category shoulders = categoryRepository.findByName("Shoulders");

                exerciseRepository.saveAll(List.of(

                        // ------------------ ABDOMEN ------------------
                        Exercise.builder().name("Ab Wheel Rollouts").description("An ab wheel rollout is a core exercise in which the trainee kneels on the floor, grips an ab wheel, and extends their body forward while maintaining a neutral spine. The movement requires engagement of the rectus abdominis, transverse abdominis, and obliques to control the rollout and return to the starting position.").gifUrl("/images/exercises/ab-wheel-rollouts.gif").sets(3).reps(12).category(abdomen).build(),
                        Exercise.builder().name("Hanging Leg Raises").description("A hanging leg raise is a core exercise in which the trainee hangs from a pull-up bar and lifts their legs by flexing the hip joint while maintaining a controlled movement. This exercise primarily engages the lower rectus abdominis and requires significant grip and upper body stability.").gifUrl("/images/exercises/hanging-leg-raises.gif").sets(3).reps(12).category(abdomen).build(),
                        Exercise.builder().name("Bicycle Crunch").description("A bicycle crunch is an abdominal exercise where the trainee lies on their back, lifts their legs, and alternates touching the opposite elbow to each knee. This movement involves spinal flexion and rotation, engaging the rectus abdominis and obliques to improve core strength.").gifUrl("/images/exercises/bicycle-crunch.gif").sets(3).reps(15).category(abdomen).build(),
                        Exercise.builder().name("Cable Crunch").description("A cable crunch is a weighted core exercise where the trainee kneels in front of a cable machine and pulls the weight down by flexing the spine. This movement emphasizes the rectus abdominis and allows for progressive overload, making it an effective strength-building ab exercise.").gifUrl("/images/exercises/cable-crunch.gif").sets(3).reps(12).category(abdomen).build(),
                        Exercise.builder().name("Plank").description("A plank is a static isometric exercise where the trainee holds a position similar to a push-up with elbows bent at 90 degrees. The core remains engaged to resist spinal extension, strengthening the abdominal muscles, lower back, and shoulders.").gifUrl("/images/exercises/plank.gif").sets(1).reps(30).category(abdomen).build(),
                        Exercise.builder().name("Russian Twists").description("A Russian twist is a rotational core exercise where the trainee sits on the floor with their feet elevated and rotates the torso from side to side while holding a weight. This movement primarily targets the obliques and improves rotational core stability.").gifUrl("/images/exercises/russian-twists.gif").sets(3).reps(15).category(abdomen).build(),
                        Exercise.builder().name("Mountain Climbers").description("Mountain climbers are a dynamic core and cardiovascular exercise where the trainee starts in a high plank position and alternates bringing their knees toward the chest. This movement engages the rectus abdominis, hip flexors, and shoulders while improving endurance.").gifUrl("/images/exercises/mountain-climbers.gif").sets(3).reps(15).category(abdomen).build(),
                        Exercise.builder().name("Side Plank").description("A side plank is a unilateral core exercise where the trainee supports their body on one forearm and the side of one foot while keeping the body straight. This movement strengthens the obliques and stabilizing muscles to enhance balance and core endurance.").gifUrl("/images/exercises/side-plank.gif").sets(1).reps(30).category(abdomen).build(),
                        Exercise.builder().name("Crunches").description("A crunch is an abdominal exercise where the trainee lies on their back with knees bent and lifts their upper torso by contracting the rectus abdominis. This movement focuses on strengthening the upper abs with controlled spinal flexion.").gifUrl("/images/exercises/crunches.gif").sets(3).reps(15).category(abdomen).build(),
                        Exercise.builder().name("Leg Raises").description("A leg raise is an abdominal exercise where the trainee lies on their back and lifts their legs while keeping them straight. The movement emphasizes the lower rectus abdominis and requires hip flexor activation.").gifUrl("/images/exercises/leg-raises.gif").sets(3).reps(12).category(abdomen).build(),

                        // ------------------ ARMS ------------------
                        Exercise.builder().name("Bicep Curl").description("A bicep curl is an upper arm exercise where the trainee lifts a weight by flexing the elbow joint. The movement isolates the biceps brachii and requires control to prevent momentum from reducing effectiveness.").gifUrl("/images/exercises/bicep_curl.gif").sets(3).reps(12).category(arms).build(),
                        Exercise.builder().name("Concentration Curl").description("A concentration curl is a unilateral biceps exercise performed by sitting with an elbow braced against the thigh, ensuring strict movement and isolation of the biceps.").gifUrl("/images/exercises/concentration_curl.gif").sets(3).reps(12).category(arms).build(),
                        Exercise.builder().name("Hammer Curl").description("A hammer curl is a curl variation where the trainee holds dumbbells in a neutral grip and lifts them by flexing the elbow joint. This movement targets both the biceps and the brachialis for overall arm development.").gifUrl("/images/exercises/hammer_curl.gif").sets(3).reps(12).category(arms).build(),
                        Exercise.builder().name("Preacher Curl").description("A preacher curl is a biceps exercise where the trainee rests their arms on a sloped bench and curls the weight upwards, preventing momentum and ensuring isolation of the biceps brachii.").gifUrl("/images/exercises/preacher.gif").sets(3).reps(12).category(arms).build(),
                        Exercise.builder().name("Skull Crushers").description("A skull crusher is a triceps isolation exercise where the trainee lies on a bench and lowers a barbell or dumbbells toward the forehead before extending the elbows to return to the starting position.").gifUrl("/images/exercises/skull_crushers.gif").sets(3).reps(12).category(arms).build(),
                        Exercise.builder().name("Tricep Dips").description("A tricep dip is a bodyweight exercise where the trainee lowers their body by bending the elbows before pushing back up, engaging the triceps and chest muscles.").gifUrl("/images/exercises/tricep_dips.gif").sets(3).reps(12).category(arms).build(),
                        Exercise.builder().name("Triceps Pushdown").description("A triceps pushdown is an isolation exercise performed on a cable machine by pushing a bar or rope downward while keeping the elbows fixed. This movement targets the triceps brachii and enhances arm strength.").gifUrl("/images/exercises/pushdown.gif").sets(3).reps(12).category(arms).build(),
                        Exercise.builder().name("Zottman Curl").description("A Zottman curl is a biceps and forearm exercise where the trainee curls a weight with a supinated grip and then lowers it with a pronated grip, maximizing arm development.").gifUrl("/images/exercises/zottman.gif").sets(3).reps(12).category(arms).build(),
                        Exercise.builder().name("Upright Rows").description("An upright row is a shoulder and upper arm exercise where the trainee pulls a barbell or dumbbells upwards while keeping the elbows higher than the hands. This movement engages the deltoids and traps.").gifUrl("/images/exercises/upright-rows.gif").sets(3).reps(12).category(arms).build(),
                        Exercise.builder().name("Weighted Chin-Up").description("A weighted chin-up is a progression of the standard chin-up where additional weight is attached to the trainee. This movement increases biceps and back strength while improving grip endurance.").gifUrl("/images/exercises/weighted-chin-up-muscles.gif").sets(3).reps(10).category(arms).build(),

                        // ------------------ BACK ------------------
                        Exercise.builder().name("Deadlift").description("A deadlift is a compound strength exercise where the trainee lifts a loaded barbell from the ground to a standing position by extending the hips and knees. This movement engages the entire posterior chain, including the erector spinae, glutes, hamstrings, and traps.").gifUrl("/images/exercises/deadlift.gif").sets(3).reps(8).category(back).build(),
                        Exercise.builder().name("Barbell Row").description("A barbell row is a pulling exercise where the trainee bends at the hips and pulls a barbell toward their torso, engaging the latissimus dorsi, rhomboids, and traps while maintaining a strong core and neutral spine.").gifUrl("/images/exercises/barbell-row.gif").sets(4).reps(10).category(back).build(),
                        Exercise.builder().name("Lat Pulldown").description("A lat pulldown is a machine-based exercise where the trainee pulls a bar down toward their chest while seated, engaging the latissimus dorsi, rear delts, and biceps to develop upper body pulling strength.").gifUrl("/images/exercises/lat-pulldown.gif").sets(3).reps(10).category(back).build(),
                        Exercise.builder().name("Dumbbell Rows").description("A dumbbell row is a unilateral pulling exercise where the trainee supports one hand on a bench while pulling a dumbbell toward their torso. This movement strengthens the lats, rhomboids, and traps while promoting muscular symmetry.").gifUrl("/images/exercises/dumbbell-rows.gif").sets(3).reps(10).category(back).build(),
                        Exercise.builder().name("Pull-Up").description("A pull-up is a bodyweight exercise where the trainee grips a bar with an overhand grip and pulls themselves upward until their chin clears the bar. This movement strengthens the lats, biceps, and upper back while improving grip endurance.").gifUrl("/images/exercises/pull-up.gif").sets(3).reps(10).category(back).build(),
                        Exercise.builder().name("Seated Cable Row").description("A seated cable row is a machine-based pulling exercise where the trainee sits with their feet braced and pulls a handle toward their torso. This movement targets the middle and lower traps, rhomboids, and lats while maintaining core engagement.").gifUrl("/images/exercises/seated-cable-row.gif").sets(3).reps(12).category(back).build(),
                        Exercise.builder().name("Weighted Chin-Up").description("A weighted chin-up is a bodyweight exercise where the trainee grips a bar with an underhand grip and pulls themselves up while wearing additional weight. This variation increases upper body pulling strength and biceps engagement.").gifUrl("/images/exercises/weighted-chin-up-muscles.gif").sets(3).reps(10).category(back).build(),
                        Exercise.builder().name("Shrugs").description("A shrug is a trap-focused exercise where the trainee lifts their shoulders toward their ears while holding weights. This movement targets the upper trapezius muscles and improves upper back stability.").gifUrl("/images/exercises/shrugs.gif").sets(3).reps(15).category(back).build(),
                        Exercise.builder().name("T-Bar Row").description("A T-bar row is a compound pulling exercise where the trainee bends at the hips and pulls a weighted barbell toward their torso using a close grip. This movement strengthens the mid-back, lats, and traps while requiring core stabilization.").gifUrl("/images/exercises/t-bar-row.gif").sets(3).reps(12).category(back).build(),
                        Exercise.builder().name("Towel Face Pull").description("A towel face pull is a cable-based exercise where the trainee pulls a rope attachment toward their face, engaging the rear delts, traps, and rotator cuff muscles to improve shoulder stability and posture.").gifUrl("/images/exercises/towel-face-pull.gif").sets(3).reps(12).category(back).build(),

                        // ------------------ CHEST ------------------
                        Exercise.builder().name("Bench Press").description("A bench press is a compound exercise where the trainee lies on a bench and presses a barbell upward by extending the arms. This movement targets the pectoralis major, anterior deltoids, and triceps.").gifUrl("/images/exercises/bench_press.gif").sets(4).reps(8).category(chest).build(),
                        Exercise.builder().name("Incline Barbell Press").description("An incline barbell press is a variation of the bench press where the trainee presses the weight from an inclined bench position, emphasizing the upper pectoral muscles and anterior deltoids.").gifUrl("/images/exercises/incline_barbell_press.gif").sets(3).reps(10).category(chest).build(),
                        Exercise.builder().name("Barbell Decline Bench Press").description("A decline bench press is a barbell pressing movement where the bench is set at a decline to target the lower portion of the pectoralis major.").gifUrl("/images/exercises/barbell-decline-bench-press.gif").sets(3).reps(10).category(chest).build(),
                        Exercise.builder().name("Pec Deck").description("A pec deck fly is a machine-based chest isolation exercise where the trainee presses the handles inward, engaging the pectoralis major to build muscle definition and separation.").gifUrl("/images/exercises/pec-deck.gif").sets(3).reps(12).category(chest).build(),
                        Exercise.builder().name("Cable Crossover").description("A cable crossover is a chest isolation exercise performed using a cable machine. The trainee pulls the handles together in an arc motion, emphasizing the contraction of the pectoral muscles.").gifUrl("/images/exercises/cable-crossover.gif").sets(3).reps(12).category(chest).build(),
                        Exercise.builder().name("Dumbbell Press").description("A dumbbell press is a free-weight pressing movement where the trainee lies on a bench and presses dumbbells upward. This variation increases the range of motion and engages stabilizing muscles.").gifUrl("/images/exercises/dumbbell-press.gif").sets(3).reps(10).category(chest).build(),
                        Exercise.builder().name("Chest Fly").description("A chest fly is an isolation exercise where the trainee holds dumbbells and moves the arms in an arc motion, stretching and contracting the pectorals.").gifUrl("/images/exercises/chest_fly.gif").sets(3).reps(12).category(chest).build(),
                        Exercise.builder().name("Close-Grip Bench Press").description("A close-grip bench press is a barbell pressing exercise performed with a narrower grip to emphasize the triceps while still engaging the chest.").gifUrl("/images/exercises/close_grip_bench.gif").sets(3).reps(10).category(chest).build(),
                        Exercise.builder().name("Push-Ups").description("A push-up is a bodyweight pressing movement where the trainee lowers and raises their body by flexing and extending the elbows. This exercise strengthens the pectorals, deltoids, and triceps.").gifUrl("/images/exercises/push-ups.gif").sets(3).reps(15).category(chest).build(),
                        Exercise.builder().name("Wide Arm Push-Ups").description("A wide-arm push-up is a variation of the standard push-up where the hands are placed further apart, placing greater emphasis on the chest muscles.").gifUrl("/images/exercises/wide-arm-push-ups.gif").sets(3).reps(15).category(chest).build(),

                        // ------------------ SHOULDERS ------------------
                        Exercise.builder().name("Arnold Press").description("An Arnold press is a shoulder pressing movement where the trainee starts with dumbbells in front of their shoulders and rotates their hands outward while pressing up. This exercise engages the anterior, lateral, and posterior deltoid heads for full shoulder development.").gifUrl("/images/exercises/arnold-press.gif").sets(3).reps(10).category(shoulders).build(),
                        Exercise.builder().name("Dumbbell Lateral Raise").description("A dumbbell lateral raise is a shoulder isolation exercise where the trainee lifts dumbbells to the sides while keeping their arms slightly bent. This movement primarily targets the lateral deltoid, enhancing shoulder width.").gifUrl("/images/exercises/DumbbellLateralRaise.gif").sets(3).reps(12).category(shoulders).build(),
                        Exercise.builder().name("Front Raises").description("A front raise is a shoulder exercise where the trainee lifts dumbbells or a barbell in front of the body with straight arms. This movement emphasizes the anterior deltoid and improves shoulder strength.").gifUrl("/images/exercises/FrontRaises.gif").sets(3).reps(12).category(shoulders).build(),
                        Exercise.builder().name("Cable Lateral Raises").description("A cable lateral raise is a resistance-based variation of the lateral raise that allows for constant tension throughout the movement, improving muscle activation in the lateral deltoid.").gifUrl("/images/exercises/cable-lateral-raises.gif").sets(3).reps(12).category(shoulders).build(),
                        Exercise.builder().name("Rear Delt Fly").description("A rear delt fly is an isolation exercise where the trainee bends forward and moves the arms outward in a reverse fly motion. This movement targets the posterior deltoid and upper back.").gifUrl("/images/exercises/RearDeltFly.gif").sets(3).reps(12).category(shoulders).build(),
                        Exercise.builder().name("Overhead Press").description("An overhead press is a compound shoulder movement where the trainee presses a barbell or dumbbells from shoulder level to an overhead position. This exercise develops strength in the deltoids, triceps, and upper chest.").gifUrl("/images/exercises/OverheadPress.gif").sets(3).reps(10).category(shoulders).build(),
                        Exercise.builder().name("Upright Rows").description("An upright row is a shoulder and upper trap exercise where the trainee pulls a barbell or dumbbells upward close to their body, keeping their elbows higher than their hands. This movement targets the lateral deltoids and trapezius.").gifUrl("/images/exercises/upright-rows.gif").sets(3).reps(12).category(shoulders).build(),
                        Exercise.builder().name("Towel Face Pull").description("A towel face pull is a rear deltoid and trap-focused movement where the trainee pulls a rope or towel attachment on a cable machine toward their face. This exercise strengthens the posterior delts and improves shoulder stability.").gifUrl("/images/exercises/towel-face-pull.gif").sets(3).reps(12).category(shoulders).build(),
                        Exercise.builder().name("Reverse Pec Deck Fly").description("A reverse pec deck fly is a machine-based shoulder exercise where the trainee moves the arms outward while seated. This movement isolates the rear deltoids and improves upper back posture.").gifUrl("/images/exercises/pec-deck.gif").sets(3).reps(12).category(shoulders).build(),
                        Exercise.builder().name("Plate Front Raise").description("A plate front raise is a shoulder exercise where the trainee lifts a weight plate from the waist to shoulder height while maintaining straight arms. This movement emphasizes the anterior deltoid and helps develop front shoulder strength.").gifUrl("/images/exercises/front-plate-raise.gif").sets(3).reps(12).category(shoulders).build(),

                        // ------------------ LEGS ------------------
                        Exercise.builder().name("Squat").description("A squat is a compound lower-body exercise where the trainee lowers their hips from a standing position and then stands back up by extending the knees and hips. This movement strengthens the quadriceps, hamstrings, glutes, and core.").gifUrl("/images/exercises/squat.gif").sets(3).reps(10).category(legs).build(),
                        Exercise.builder().name("Romanian Deadlifts").description("A Romanian deadlift is a posterior-chain exercise where the trainee lowers a barbell or dumbbells by hinging at the hips while maintaining a slight knee bend. This movement primarily targets the hamstrings and glutes.").gifUrl("/images/exercises/RomanianDeadlifts.gif").sets(3).reps(10).category(legs).build(),
                        Exercise.builder().name("Leg Press").description("A leg press is a machine-based lower-body exercise where the trainee pushes a weighted platform away from their body by extending the knees. This movement targets the quadriceps, hamstrings, and glutes while reducing spinal load.").gifUrl("/images/exercises/leg-press.gif").sets(3).reps(12).category(legs).build(),
                        Exercise.builder().name("Bulgarian Split Squat").description("A Bulgarian split squat is a single-leg exercise where the trainee places one foot on an elevated surface and performs a squat with the front leg. This movement improves balance, coordination, and unilateral leg strength.").gifUrl("/images/exercises/bulgarian-split-spuat.gif").sets(3).reps(10).category(legs).build(),
                        Exercise.builder().name("Hip Thrust").description("A hip thrust is a glute-dominant exercise where the trainee rests their upper back on a bench and extends their hips upward while holding a barbell. This movement strengthens the glutes, hamstrings, and lower back.").gifUrl("/images/exercises/hip-thrust.gif").sets(3).reps(10).category(legs).build(),
                        Exercise.builder().name("Lunges").description("A lunge is a unilateral leg exercise where the trainee steps forward or backward and lowers the rear knee toward the ground before returning to the starting position. This movement targets the quadriceps, glutes, and hamstrings.").gifUrl("/images/exercises/lunges.gif").sets(3).reps(12).category(legs).build(),
                        Exercise.builder().name("Calf Raises").description("A calf raise is a lower-leg exercise where the trainee lifts their heels off the ground by plantar flexing the ankles. This movement strengthens the gastrocnemius and soleus muscles.").gifUrl("/images/exercises/CalfRaises.gif").sets(3).reps(15).category(legs).build(),
                        Exercise.builder().name("Hamstring Curls").description("A hamstring curl is a machine-based exercise where the trainee flexes their knees to bring the heels toward the glutes while lying or seated. This movement isolates the hamstrings for improved strength and definition.").gifUrl("/images/exercises/HamstringCurls.gif").sets(3).reps(10).category(legs).build(),
                        Exercise.builder().name("Step-Ups").description("A step-up is a unilateral leg exercise where the trainee steps onto an elevated platform while maintaining control and balance. This movement strengthens the quadriceps, glutes, and hamstrings.").gifUrl("/images/exercises/step-ups.gif").sets(3).reps(12).category(legs).build(),
                        Exercise.builder().name("Deadlift").description("A deadlift is a compound movement where the trainee lifts a loaded barbell from the ground to a standing position using hip and knee extension. This exercise strengthens the entire posterior chain, including the glutes, hamstrings, and lower back.").gifUrl("/images/exercises/deadlift.gif").sets(3).reps(8).category(legs).build()

                ));
            }
        };
    }
}
