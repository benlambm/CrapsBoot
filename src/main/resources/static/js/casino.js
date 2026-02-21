document.addEventListener('DOMContentLoaded', function () {
    const diceFaces = ['\u2680', '\u2681', '\u2682', '\u2683', '\u2684', '\u2685'];

    // ===== Page-load fade-in animations =====
    document.querySelectorAll('.fade-in').forEach(function (el) {
        // Small delay so the initial opacity:0 is visible before animating
        requestAnimationFrame(function () {
            el.classList.add('loaded');
        });
    });

    // ===== Win/Loss message detection =====
    var messageEl = document.getElementById('game-message');
    if (messageEl) {
        var text = messageEl.textContent.toLowerCase();
        if (text.includes('win')) {
            messageEl.classList.add('win-message');
        } else if (text.includes('lose') || text.includes('seven out') || text.includes('craps')) {
            messageEl.classList.add('lose-message');
        }
    }

    // ===== Roll button interception with dice animation =====
    var rollForm = document.getElementById('roll-form');
    var rollBtn = document.getElementById('roll-btn');
    var dice1 = document.getElementById('dice1');
    var dice2 = document.getElementById('dice2');

    if (rollForm && rollBtn && dice1 && dice2) {
        rollForm.addEventListener('submit', function (e) {
            e.preventDefault();

            // Disable button and change text
            rollBtn.classList.remove('pulse-glow');
            rollBtn.classList.add('rolling-btn');
            rollBtn.textContent = 'Rolling...';
            rollBtn.disabled = true;

            // Start dice shaking
            dice1.classList.add('shaking');
            dice2.classList.add('shaking');

            // Cycle dice faces rapidly
            var cycleInterval = setInterval(function () {
                dice1.textContent = diceFaces[Math.floor(Math.random() * 6)];
                dice2.textContent = diceFaces[Math.floor(Math.random() * 6)];
            }, 80);

            // After delay, stop animation and submit form
            setTimeout(function () {
                clearInterval(cycleInterval);
                dice1.classList.remove('shaking');
                dice2.classList.remove('shaking');
                rollForm.submit();
            }, 1500);
        });
    }

    // ===== Leaderboard row stagger =====
    document.querySelectorAll('.slide-in-row').forEach(function (row, index) {
        row.style.animationDelay = (index * 0.15) + 's';
    });
});
