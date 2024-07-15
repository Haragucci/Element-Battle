document.addEventListener("DOMContentLoaded", function() {
    const createProfilesButton = document.getElementById("create-profiles-button");

    createProfilesButton.addEventListener("click", function() {
        fetch("/createProfiles")
            .then(response => response.text())
            .then(data => {
                createProfilesButton.classList.add('blink-success');
                setTimeout(() => createProfilesButton.classList.remove('blink-success'), 1000);
            })
            .catch(error => {
                console.error("Fehler beim Erstellen der Profile:", error);
            });
    });

    document.getElementById('hero-form').addEventListener('submit', function(event) {
        event.preventDefault();

        const nameInput = document.getElementById('name');
        const name = nameInput.value;
        const HP = document.getElementById('HP').value;
        const Damage = document.getElementById('Damage').value;
        const type = document.getElementById('type').value;
        const extra = document.getElementById('extra').value;

        if (!name) {
            alert('Name darf nicht leer sein!');
            nameInput.classList.add('blink-error');
            setTimeout(() => nameInput.classList.remove('blink-error'), 1000);
            return;
        }

        const hero = {
            name: name,
            HP: parseInt(HP) || 0,
            Damage: parseInt(Damage) || 0,
            type: type,
            extra: extra
        };
        if (hero.HP <= 0){
            hero.HP = 0;
        }

        fetch('/hero', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(hero)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                console.log('Success:', data);
                document.getElementById('hero-form').reset();
                const formContainer = document.querySelector('.form-container');
                formContainer.classList.remove('blink-error');
                formContainer.classList.add('blink-success');
                setTimeout(() => formContainer.classList.remove('blink-success'), 1000);
            })
            .catch((error) => {
                console.error('Error:', error);
                const formContainer = document.querySelector('.form-container');
                formContainer.classList.remove('blink-success');
                formContainer.classList.add('blink-error');
                setTimeout(() => formContainer.classList.remove('blink-error'), 1000);
            });
    });
});
