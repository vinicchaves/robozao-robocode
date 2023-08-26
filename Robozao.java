package robonovo;
import robocode.*;
import java.awt.Color;
import robocode.util.Utils;

public class Robozao extends AdvancedRobot {

    private double lastEnemyBearing = 0;
    private double lastEnemyDistance = 0;
    private boolean isMovingForward = true;
    private double moveDirection = 1.0;
    private int randomMoveCounter = 0;
    private int randomMoveInterval = 10;

    @Override
    public void run() {
        setColors(Color.red, Color.black, Color.yellow);
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        while (true) {
            moverEvasivamente(); // Movimentação evasiva
            execute();
        }
    }

    private void moverEvasivamente() {
        double margemParede = 80; // Margem de segurança em relação às paredes

        // Lógica para movimento evasivo e mudança de direção
        if (getOthers() > 1 && getTime() % 20 == 0) {
            isMovingForward = !isMovingForward;
            moveDirection *= -1;
        }

        randomMoveCounter++;
        if (randomMoveCounter >= randomMoveInterval) {
            isMovingForward = !isMovingForward;
            moveDirection = Math.random() > 0.5 ? 1.0 : -1.0;
            randomMoveCounter = 0;
        }

        double x = getX();
        double y = getY();
        double larguraCampo = getBattleFieldWidth();
        double alturaCampo = getBattleFieldHeight();
	

        // Lógica para evitar colisões com as paredes
	if (x <= margemParede || x >= larguraCampo - margemParede ||
        y <= margemParede || y >= alturaCampo - margemParede) {
        double randomAngle = Math.random() * 90 - 45; // Adicionar ângulo aleatório à direção
        setTurnRight(90 * moveDirection + randomAngle);
        setAhead(150 * moveDirection);
    } else {
        if (isMovingForward) {
            setAhead(100 * moveDirection);
        } else {
            setBack(100 * moveDirection);
        }
    }

	
        setTurnGunRight(45 * moveDirection);
        setTurnRadarRight(360);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double bearingInimigo = event.getBearing();
        double distanciaInimigo = event.getDistance();
        double direcaoAbsoluta = getHeading() + bearingInimigo;
		double velocidadeInimigo = event.getVelocity();

        bearingInimigo = lastEnemyBearing;
        distanciaInimigo = lastEnemyDistance;
		
    // Rastrear o inimigo com o radar
    double radarTurn = Utils.normalRelativeAngleDegrees(direcaoAbsoluta - getRadarHeading());
    radarTurn += radarTurn < 0 ? -10 : 10; // melhorar a movimentação do radar
    setTurnRadarRight(radarTurn);
	
        // Ajustar a mira para o inimigo
        double gunTurn = Utils.normalRelativeAngleDegrees(direcaoAbsoluta - getGunHeading());
        setTurnGunRight(gunTurn);	
   


        // Lógica para cercar o inimigo
        double distanciaOrbita = 400; // Distância de órbita desejada
        double orbitaDesejada = 400; // Distância alvo de órbita desejada

        double diferencaOrbita = distanciaInimigo - orbitaDesejada;
        double quantidadeGiro = 5; // Quantidade de giro

        // Ajusta a direção do movimento para manter a distância de órbita desejada
        if (diferencaOrbita > 0) {
            setAhead(quantidadeGiro);
        } else {
            setBack(quantidadeGiro);
        }

        double potenciaTiro = Math.min(3, 400 / distanciaInimigo);
        if (velocidadeInimigo > 0) {
            potenciaTiro = Math.min(potenciaTiro, 2);
        }
        
        // Ajustar a mira para distâncias maiores
        if (distanciaInimigo < 800) {
            fire(potenciaTiro);
        }

        if (lastEnemyDistance > 0) {
            double mudancaDistancia = distanciaInimigo - lastEnemyDistance;
            if (mudancaDistancia > 0) {
                potenciaTiro = Math.min(1, 400 / distanciaInimigo);
                if (velocidadeInimigo > 0) {
                    potenciaTiro = Math.min(potenciaTiro, 1);
                }
                fire(potenciaTiro);
            }
        }

        // Lógica para movimento evasivo quando o inimigo atira
        if (distanciaInimigo < 300 && getEnergy() > 50) {
            isMovingForward = !isMovingForward;
            moveDirection *= -1;
            double giroAleatorio = Math.random() * 180 - 90; // Giro aleatório entre -90 e 90 graus
            setTurnRight(giroAleatorio);
        }
		

    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        isMovingForward = !isMovingForward;
        setTurnRight(event.getBearing() + 90);
        setAhead(150);
        setTurnRight(45 * (Math.random() - 0.5));
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        isMovingForward = !isMovingForward;
        double margemParede = 100; // Margem de segurança em relação às paredes
        double quantidadeMovimento = isMovingForward ? -150 : 150;

        // Lógica para evitar colisões com as paredes
        if (getX() < margemParede || getX() > getBattleFieldWidth() - margemParede ||
            getY() < margemParede || getY() > getBattleFieldHeight() - margemParede) {
            setTurnRight(180);
            setAhead(100);
        } else {
            setTurnRight(90);
            setAhead(150);
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        if (event.isMyFault()) {
            isMovingForward = !isMovingForward;
            double distanciaEstimada = event.getEnergy() * 10;

            if (distanciaEstimada < 50) {
                setBack(150); // Recuar mais se a estimativa de distância for pequena
            } else {
                setBack(100);
            }
        }
    }
	

}