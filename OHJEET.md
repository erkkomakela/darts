# Darts-sovellus — Asennusohjeet

## 1. Luo GitHub-repositorio

1. Mene github.com → New repository
2. Nimi: `darts`
3. Public tai Private — kumpi tahansa
4. **ÄLÄ** lisää README:ta (projekti on jo valmis)

## 2. Vie koodi GitHubiin

Avaa terminaali tässä kansiossa ja aja:

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/SINUN-KÄYTTÄJÄNIMI/darts.git
git push -u origin main
```

(Korvaa SINUN-KÄYTTÄJÄNIMI omalla GitHub-käyttäjänimelläsi)

## 3. Luo allekirjoitusavain (keystore)

**Vaihtoehto A — helpoin (GitHub buildaa unsigned APK:n):**

Muokkaa `.github/workflows/build.yml` — poista Sign APK -vaihe ja muuta Upload APK:
```yaml
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: darts-release
          path: app/build/outputs/apk/release/app-release-unsigned.apk
```

**Vaihtoehto B — allekirjoitettu APK:**

1. Asenna JDK jos ei ole: https://adoptium.net
2. Aja: `bash generate_keystore.sh`
3. Muunna keystore base64:ksi:
   - Windows: `certutil -encode darts-release-key.jks key.txt`
   - Kopioi key.txt sisältö (ilman header/footer rivejä)
4. GitHub → Settings → Secrets and variables → Actions → New secret:
   - `SIGNING_KEY` = base64-sisältö
   - `KEY_ALIAS` = darts
   - `KEY_STORE_PASSWORD` = dartsapp123
   - `KEY_PASSWORD` = dartsapp123

## 4. Lataa APK

1. Mene GitHub-repositorioon
2. Klikkaa "Actions" välilehteä
3. Klikkaa viimeisin "Build APK" ajo
4. Sivun alareunassa "Artifacts" → lataa `darts-release`
5. Pura zip → saat APK-tiedoston

## 5. Asenna puhelimeen

1. Kopioi APK puhelimeen (USB tai sähköposti)
2. Puhelin → Asetukset → Turvallisuus → Salli tuntemattomista lähteistä
3. Avaa APK tiedostonhallinnasta → Asenna

---

## Päivittäminen myöhemmin

Kun teet muutoksia koodiin:
```bash
git add .
git commit -m "Muutos"
git push
```
GitHub buildaa uuden APK:n automaattisesti!
