let selectedExercises = [];
const modal = document.getElementById('exerciseModal');

document.addEventListener('DOMContentLoaded', () => {
    const categoryLinks = document.querySelectorAll('.category-link');
    const startWorkoutButton = document.getElementById('startWorkoutButton');
    const exerciseList = document.getElementById('exerciseList');

    categoryLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const categoryName = link.dataset.name;
            if (categoryName) {
                fetchExercises(categoryName);
            }
        });
    });

    function fetchExercises(categoryName) {
        fetch(`/workout/exercise?categoryName=${categoryName}`)
            .then(response => response.json())
            .then(data => {
                exerciseList.innerHTML = '';

                if (!data || data.length === 0) {
                    exerciseList.innerHTML = '<li>No exercises found for this category.</li>';
                } else {
                    data.forEach(exercise => {
                        const li = document.createElement('li');
                        li.classList.add('exercise-item');
                        li.dataset.exerciseId = exercise.id;
                        li.textContent = exercise.name;

                        li.addEventListener('click', () => {
                            toggleExerciseSelection(li);
                        });

                        exerciseList.appendChild(li);
                    });
                }
                showModal();
            })
            .catch(err => console.error('Error fetching exercises:', err));
    }

    function toggleExerciseSelection(element) {
        const exerciseId = element.dataset.exerciseId;

        if (selectedExercises.includes(exerciseId)) {
            selectedExercises = selectedExercises.filter(id => id !== exerciseId);
            element.classList.remove('selected-exercise');
        } else {
            selectedExercises.push(exerciseId);
            element.classList.add('selected-exercise');
        }

        console.log("Updated selectedExercises:", selectedExercises);
        startWorkoutButton.disabled = selectedExercises.length === 0;
    }
});

// ✅ Make functions global so they can be called from HTML
window.showModal = function () {
    modal.style.display = 'flex';

    // Close modal when clicking outside the content
    modal.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });
};

window.closeModal = function () {
    modal.style.display = 'none';
};

window.startWorkout = function () {
    if (selectedExercises.length === 0) {
        alert("Please select at least one exercise.");
        return;
    }

    fetch("/workout/selectExercises", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ selectedExercises })
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text); });
            }
            return response.text();
        })
        .then(data => {
            console.log("Backend response:", data);
            window.location.href = "/workout/startWorkout";
        })
        .catch(err => {
            console.error("Failed to start workout:", err);
            alert(`Failed to start workout: ${err.message}`);
        });
};
