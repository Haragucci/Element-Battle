document.addEventListener("DOMContentLoaded", function() {
    const createProfilesButton = document.getElementById("create-profiles-button");

    createProfilesButton.addEventListener("click", function() {
        fetch("/createProfiles")
            .then(response => response.text())
            .then(data=> {
                data.includes("");//Nur damit keine Fehler angezeigt werden
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
                formContainer.classList.add('blink-success');
                const successMessage = document.createElement('div');
                successMessage.textContent = 'Karte erfolgreich Erstellt!';
                successMessage.style.position = 'fixed';
                successMessage.style.top = '200px';
                successMessage.style.left = '50%';
                successMessage.style.transform = 'translateX(-50%)';
                successMessage.style.backgroundColor = '#4CAF50';
                successMessage.style.color = 'white';
                successMessage.style.padding = '10px 20px';
                successMessage.style.borderRadius = '5px';
                successMessage.style.zIndex='10000'
                document.body.appendChild(successMessage);

                setTimeout(() => {
                    document.body.removeChild(successMessage);
                }, 3000);
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
