document.addEventListener('DOMContentLoaded', function () {
    const avatar = document.querySelector('.avatar');
    const dropdown = document.querySelector('.avatar .dropdown');

    if (avatar && dropdown) {
        avatar.addEventListener('click', function (e) {
            e.stopPropagation();
            dropdown.classList.toggle('show');
        });

        document.addEventListener('click', function () {
            dropdown.classList.remove('show');
        });

        dropdown.addEventListener('click', function (e) {
            e.stopPropagation();
        });
    }
});