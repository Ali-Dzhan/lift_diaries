document.addEventListener("DOMContentLoaded", function () {
    const statsChartElement = document.getElementById("statsChart");
    const userId = statsChartElement.getAttribute("data-user-id");

    fetch('/api/stats/' + userId)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("Fetched data:", data);

            const ctx = statsChartElement.getContext('2d');
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: ['Total Workouts', 'Longest Streak', 'Sets This Week'],
                    datasets: [{
                        label: 'Workout Statistics',
                        data: [data.totalWorkouts, data.longestStreak, data.setsThisWeek],
                        backgroundColor: ['rgba(2,140,243,0.7)', 'rgba(253,204,0,0.7)', 'rgba(214,51,51,0.7)'],
                        borderColor: ['blue', 'darkorange', 'red'],
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        title: { display: true, text: 'Statistics' }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                stepSize: 2
                            }
                        }
                    }
                }
            });
        })
        .catch(error => {
            console.error("Error fetching stats:", error);
        });
});