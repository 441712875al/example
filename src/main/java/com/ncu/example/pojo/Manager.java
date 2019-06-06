package com.ncu.example.pojo;


import com.ncu.example.dao.PTDaoImpl;
import com.ncu.example.dao.PlayerDaoImpl;
import com.ncu.example.dao.TeamDaoImpl;
import com.ncu.example.pojo.ContestType;
import com.ncu.example.pojo.GroupStrategy;
import com.ncu.example.pojo.Player;
import com.ncu.example.pojo.Team;
import com.ncu.example.view.PersonScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class Manager {
    private List<Player> players;
    private List<Team> teams;

    @Autowired
    private GroupStrategy groupStrategy;


    @Autowired
    private PTDaoImpl ptDaoImpl;

    @Autowired
    private TeamDaoImpl teamDaoImpl;

    @Autowired
    private PlayerDaoImpl playerDaoImpl;




    /**
     * 参赛选手报名
     * @param pid
     * @param name
     */
    public void register(int pid,String name){
        if(players == null)
            players = new ArrayList<>();
        Player newPlayer = new Player(pid,name);
        players.add(newPlayer);
        savePlayer(newPlayer);
    }



    /**
     * 为每个参赛选手分组
     * @param contestType
     */
    public void group(ContestType contestType){
        groupStrategy.setPlayers(players);
        groupStrategy.setContestType(contestType);

        //将新产生的分组加入到teams中
        setTeams(groupStrategy.group());
    }


    //根据选手每次出手击倒的瓶数进行分数统计
    public void addScore(){
        teams.forEach(team->{
            for(Player e:team.getMembers()){
                List<Integer>[] grades= e.play();
                for(int i=0;i<10;i++){
                    for(Integer o:grades[i])
                        e.getScores()[i]+=o;

                    int count = 0;
                    if(grades[i].size()==1) count = 2;
                    else if(e.getScores()[i]==10) count = 1;

                    for(int j=i+1>9?9:i+1;count>0;j++){
                        int k=0;
                        if(j==9) k++;
                        for(;k<grades[j].size();k++){
                            e.getScores()[i]+=grades[j].get(k);
                            count--;
                            if(count<=0)
                                break;
                        }
                    }
                }
            }
        });

        savePt();//将小组成员的分数保存
    }


    /**
     * 保存参赛选手信息
     * @param
     */
    public void savePlayer(Player player){
        playerDaoImpl.insertPlayer(player);
    }



    /**
     * 裁判将小组的信息保存到team表中
     * @param
     */
    public void saveTeams(){
        teams.forEach(e->teamDaoImpl.insertTeam(e));
    }

    /**
     * 裁判保存每个小组参赛队员的成绩
     */
    public void savePt()  {
        teams.forEach(e->{
            for(Player o:e.getMembers()){
                  ptDaoImpl.insertGrade(e);
            }
        });
   }


//   public JSONObject getEliteGrade(int pid, String name){
//        int tolScoreTmp = 0;
//        SqlRowSet rs = ptDaoImpl.findPlayerGrade(pid,name);
//        while (rs.next()){
//            tolScoreTmp +=rs.getInt("tolScore");
//        }
//
//        JSONObject obj = new JSONObject();
//        obj.put("pid",pid);
//        obj.put("name",name);
//        obj.put("tolScore",tolScoreTmp);
//        obj.put("contestType",ContestType.ELITE.getDesc());
//        return obj;
//   }
//
//   public PersonScore getGrade


    /**
     * 对每个小组进行的赛事进行排名
     * @param
     * @return
     */
//    public JSONArray ranking(ContestType contestType){
////        return PTDaoImpl.getInstance().findTeamGrade(contestType);
//        return ptDaoImpl.findTeamGrade(contestType);
//    }


    public List<PersonScore> findGrade(int pid,String name){
        return ptDaoImpl.findPlayerGrade(pid,name);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public GroupStrategy getGroupStrategy() {
        return groupStrategy;
    }

    public void setGroupStrategy(GroupStrategy groupStrategy) {
        this.groupStrategy = groupStrategy;
    }

    public PTDaoImpl getPtDaoImpl() {
        return ptDaoImpl;
    }

    public TeamDaoImpl getTeamDaoImpl() {
        return teamDaoImpl;
    }

    public PlayerDaoImpl getPlayerDaoImpl() {
        return playerDaoImpl;
    }
}
