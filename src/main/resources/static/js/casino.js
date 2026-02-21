document.addEventListener('DOMContentLoaded', function () {
    var diceFaces = ['\u2680', '\u2681', '\u2682', '\u2683', '\u2684', '\u2685'];

    // ===== Sound System (Web Audio API) =====
    var audioCtx = null;
    var soundEnabled = localStorage.getItem('craps-sound') !== 'muted';

    function getAudioCtx() {
        if (!audioCtx) {
            audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        }
        return audioCtx;
    }

    function playTone(freq, duration, type, gainVal) {
        if (!soundEnabled) return;
        try {
            var ctx = getAudioCtx();
            var osc = ctx.createOscillator();
            var gain = ctx.createGain();
            osc.type = type || 'sine';
            osc.frequency.value = freq;
            gain.gain.setValueAtTime(gainVal || 0.3, ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration);
            osc.connect(gain);
            gain.connect(ctx.destination);
            osc.start();
            osc.stop(ctx.currentTime + duration);
        } catch (e) { /* ignore audio errors */ }
    }

    function playNoise(duration, gainVal) {
        if (!soundEnabled) return;
        try {
            var ctx = getAudioCtx();
            var bufferSize = ctx.sampleRate * duration;
            var buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
            var data = buffer.getChannelData(0);
            for (var i = 0; i < bufferSize; i++) {
                data[i] = (Math.random() * 2 - 1) * 0.5;
            }
            var source = ctx.createBufferSource();
            source.buffer = buffer;
            var gain = ctx.createGain();
            gain.gain.setValueAtTime(gainVal || 0.2, ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration);
            // Bandpass for dice rattle sound
            var filter = ctx.createBiquadFilter();
            filter.type = 'bandpass';
            filter.frequency.value = 3000;
            filter.Q.value = 0.5;
            source.connect(filter);
            filter.connect(gain);
            gain.connect(ctx.destination);
            source.start();
        } catch (e) { /* ignore audio errors */ }
    }

    var sounds = {
        roll: function () {
            // Dice rattle: short noise bursts
            for (var i = 0; i < 6; i++) {
                setTimeout(function () { playNoise(0.08, 0.15); }, i * 120);
            }
        },
        win: function () {
            // Ascending chime
            playTone(523, 0.15, 'sine', 0.3);
            setTimeout(function () { playTone(659, 0.15, 'sine', 0.3); }, 100);
            setTimeout(function () { playTone(784, 0.3, 'sine', 0.4); }, 200);
        },
        lose: function () {
            // Descending low buzz
            playTone(300, 0.3, 'sawtooth', 0.15);
            setTimeout(function () { playTone(200, 0.5, 'sawtooth', 0.1); }, 200);
        },
        pointSet: function () {
            // Single bell ding
            playTone(880, 0.4, 'sine', 0.25);
        },
        gameOver: function () {
            // Dramatic descending sting
            playTone(440, 0.3, 'square', 0.2);
            setTimeout(function () { playTone(370, 0.3, 'square', 0.2); }, 250);
            setTimeout(function () { playTone(311, 0.3, 'square', 0.2); }, 500);
            setTimeout(function () { playTone(220, 0.8, 'sawtooth', 0.15); }, 750);
        }
    };

    function playSound(name) {
        if (!soundEnabled || !sounds[name]) return;
        sounds[name]();
    }

    // Mute toggle button
    var muteBtn = document.getElementById('mute-toggle');
    if (muteBtn) {
        muteBtn.textContent = soundEnabled ? '\uD83D\uDD0A' : '\uD83D\uDD07';
        muteBtn.addEventListener('click', function () {
            soundEnabled = !soundEnabled;
            localStorage.setItem('craps-sound', soundEnabled ? 'enabled' : 'muted');
            muteBtn.textContent = soundEnabled ? '\uD83D\uDD0A' : '\uD83D\uDD07';
            // Resume audio context on user interaction
            if (soundEnabled && audioCtx && audioCtx.state === 'suspended') {
                audioCtx.resume();
            }
        });
    }

    // ===== Page-load fade-in animations =====
    document.querySelectorAll('.fade-in').forEach(function (el) {
        requestAnimationFrame(function () {
            el.classList.add('loaded');
        });
    });

    // ===== Win/Loss message detection =====
    var messageEl = document.getElementById('game-message');
    if (messageEl) {
        var text = messageEl.textContent.toLowerCase();
        if (text.includes('won')) {
            messageEl.classList.add('win-message');
            playSound('win');
        } else if (text.includes('lost') || text.includes('seven out') || text.includes('craps')) {
            messageEl.classList.add('lose-message');
            playSound('lose');
        } else if (text.includes('point is set')) {
            playSound('pointSet');
        }
    }

    // Game over page sound
    if (document.querySelector('.slam-in')) {
        playSound('gameOver');
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

            // Play dice roll sound
            playSound('roll');

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

    // ===== Bet Chip Selection =====
    window.selectBet = function (chipEl) {
        var amount = parseInt(chipEl.getAttribute('data-bet'));
        document.querySelectorAll('.bet-chip').forEach(function (c) {
            c.classList.remove('btn-warning');
            c.classList.add('btn-outline-warning');
        });
        chipEl.classList.remove('btn-outline-warning');
        chipEl.classList.add('btn-warning');
        var betInput = document.getElementById('bet-value');
        var customInput = document.getElementById('custom-bet');
        if (betInput) betInput.value = amount;
        if (customInput) customInput.value = amount;
        if (rollBtn) rollBtn.textContent = 'Roll Dice ($' + amount + ')';
    };

    // Sync custom bet input
    var customBetInput = document.getElementById('custom-bet');
    if (customBetInput) {
        customBetInput.addEventListener('change', function () {
            var val = parseInt(this.value) || 10;
            var betInput = document.getElementById('bet-value');
            if (betInput) betInput.value = val;
            document.querySelectorAll('.bet-chip').forEach(function (c) {
                c.classList.remove('btn-warning');
                c.classList.add('btn-outline-warning');
                if (parseInt(c.getAttribute('data-bet')) === val) {
                    c.classList.remove('btn-outline-warning');
                    c.classList.add('btn-warning');
                }
            });
            if (rollBtn) rollBtn.textContent = 'Roll Dice ($' + val + ')';
        });
    }

    // ===== Leaderboard row stagger =====
    document.querySelectorAll('.slide-in-row').forEach(function (row, index) {
        row.style.animationDelay = (index * 0.15) + 's';
    });
});
